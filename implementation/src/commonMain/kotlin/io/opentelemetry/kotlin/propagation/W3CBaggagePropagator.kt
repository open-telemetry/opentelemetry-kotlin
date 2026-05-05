package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageEntry
import io.opentelemetry.kotlin.baggage.BaggageEntryMetadataImpl
import io.opentelemetry.kotlin.baggage.BaggageImpl
import io.opentelemetry.kotlin.context.Context

/**
 * W3C Baggage HTTP header propagator.
 *
 * https://www.w3.org/TR/baggage/
 */
@OptIn(ExperimentalApi::class)
internal object W3CBaggagePropagator : TextMapPropagator {

    private const val FIELD = "baggage"
    private const val ENTRY_DELIMITER = ','
    private const val KEY_VALUE_DELIMITER = '='
    private const val METADATA_DELIMITER = ';'
    private const val PERCENT_CHAR = '%'
    private const val SPACE = ' '
    private const val HTAB = '\t'
    private const val MAX_HEADER_BYTES = 8192
    private const val MAX_ENTRY_BYTES = 4096

    private val FIELDS = listOf(FIELD)

    override fun fields(): Collection<String> = FIELDS

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        val entries = context.extractBaggage().asMap()
        if (entries.isEmpty()) {
            return
        }
        val header = encode(entries) ?: return
        setter.set(carrier, FIELD, header)
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        val combined = getter.getAll(carrier, FIELD).joinToString(ENTRY_DELIMITER.toString())
        if (combined.isEmpty()) {
            return context
        }
        val baggage = decode(combined) ?: return context
        return context.storeBaggage(baggage)
    }

    private fun encode(entries: Map<String, BaggageEntry>): String? {
        val builder = StringBuilder()
        for ((name, entry) in entries) {
            val piece = encodeIfFits(name, entry, builder.length) ?: continue
            if (builder.isNotEmpty()) {
                builder.append(ENTRY_DELIMITER)
            }
            builder.append(piece)
        }
        return builder.takeIf(StringBuilder::isNotEmpty)?.toString()
    }

    private fun encodeIfFits(name: String, entry: BaggageEntry, currentLength: Int): String? {
        val piece = encodeEntry(name, entry.value, entry.metadata.value)
        if (piece.length > MAX_ENTRY_BYTES) {
            return null
        }
        val separator = when (currentLength) {
            0 -> 0
            else -> 1
        }
        if (currentLength + separator + piece.length > MAX_HEADER_BYTES) {
            return null
        }
        return piece
    }

    private fun encodeEntry(key: String, value: String, metadata: String): String {
        val sb = StringBuilder()
        sb.append(key).append(KEY_VALUE_DELIMITER).append(percentEncode(value))
        if (metadata.isNotEmpty()) {
            sb.append(METADATA_DELIMITER).append(metadata)
        }
        return sb.toString()
    }

    private fun decode(header: String): Baggage? {
        var baggage: Baggage = BaggageImpl.EMPTY
        for (rawElement in header.split(ENTRY_DELIMITER)) {
            val parsed = parseElement(rawElement) ?: continue
            baggage = baggage.set(parsed.key, parsed.value, BaggageEntryMetadataImpl(parsed.metadata))
        }
        return baggage.takeIf { it !== BaggageImpl.EMPTY }
    }

    private fun parseElement(rawElement: String): ParsedEntry? {
        val element = rawElement.trim(SPACE, HTAB)
        if (element.isEmpty()) {
            return null
        }
        return decodeEntry(element)
    }

    private fun decodeEntry(element: String): ParsedEntry? {
        val firstSemi = element.indexOf(METADATA_DELIMITER)
        val keyValuePart: String
        val metadata: String
        if (firstSemi == -1) {
            keyValuePart = element
            metadata = ""
        } else {
            keyValuePart = element.substring(0, firstSemi)
            metadata = element.substring(firstSemi + 1)
        }
        val eq = keyValuePart.indexOf(KEY_VALUE_DELIMITER)
        if (eq <= 0) {
            return null
        }
        val key = keyValuePart.substring(0, eq).trim(SPACE, HTAB)
        val rawValue = keyValuePart.substring(eq + 1).trim(SPACE, HTAB)
        val value = percentDecode(rawValue) ?: return null
        return ParsedEntry(key, value, metadata.trim(SPACE, HTAB))
    }

    private class ParsedEntry(val key: String, val value: String, val metadata: String)

    /**
     * Percent-encode bytes outside `baggage-octet`. The `%` byte is also encoded so the
     * decoder can unambiguously distinguish literals from `%HH` sequences.
     *
     * baggage-octet = %x21 / %x23-2B / %x2D-3A / %x3C-5B / %x5D-7E
     */
    private fun percentEncode(value: String): String {
        val sb = StringBuilder(value.length)
        for (byte in value.encodeToByteArray()) {
            val b = byte.toInt() and BYTE_MASK
            if (b == PERCENT || !isBaggageOctet(b)) {
                sb.append(PERCENT_CHAR)
                sb.append(HEX[b ushr HEX_SHIFT])
                sb.append(HEX[b and HEX_MASK])
            } else {
                sb.append(b.toChar())
            }
        }
        return sb.toString()
    }

    private fun percentDecode(value: String): String? {
        if (!value.contains(PERCENT_CHAR)) {
            return value
        }
        val bytes = ByteArray(value.length)
        var pos = 0
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == PERCENT_CHAR) {
                val decoded = decodeHexPair(value, i) ?: return null
                bytes[pos++] = decoded.toByte()
                i += PERCENT_SEQUENCE_LENGTH
            } else {
                bytes[pos++] = c.code.toByte()
                i++
            }
        }
        return bytes.decodeToString(0, pos)
    }

    private fun decodeHexPair(value: String, start: Int): Int? {
        if (start + 2 >= value.length) {
            return null
        }
        val hi = hexDigit(value[start + 1])
        val lo = hexDigit(value[start + 2])
        if (hi < 0 || lo < 0) {
            return null
        }
        return (hi shl HEX_SHIFT) or lo
    }

    private fun isBaggageOctet(b: Int): Boolean = when (b) {
        OCTET_EXCLAIM -> true
        in OCTET_HASH..OCTET_PLUS -> true
        in OCTET_DASH..OCTET_COLON -> true
        in OCTET_LT..OCTET_LBRACKET -> true
        in OCTET_RBRACKET..OCTET_TILDE -> true
        else -> false
    }

    private fun hexDigit(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + DECIMAL_BASE
        in 'A'..'F' -> c - 'A' + DECIMAL_BASE
        else -> -1
    }

    private const val PERCENT = 0x25
    private const val BYTE_MASK = 0xFF
    private const val HEX_SHIFT = 4
    private const val HEX_MASK = 0xF
    private const val DECIMAL_BASE = 10
    private const val PERCENT_SEQUENCE_LENGTH = 3
    private const val OCTET_EXCLAIM = 0x21
    private const val OCTET_HASH = 0x23
    private const val OCTET_PLUS = 0x2B
    private const val OCTET_DASH = 0x2D
    private const val OCTET_COLON = 0x3A
    private const val OCTET_LT = 0x3C
    private const val OCTET_LBRACKET = 0x5B
    private const val OCTET_RBRACKET = 0x5D
    private const val OCTET_TILDE = 0x7E

    private val HEX = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    )
}
