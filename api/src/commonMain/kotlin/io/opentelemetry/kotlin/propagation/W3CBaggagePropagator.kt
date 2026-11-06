package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageEntry
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.propagation.utils.isValidBaggageKey
import io.opentelemetry.kotlin.propagation.utils.percentDecodeBaggageValue
import io.opentelemetry.kotlin.propagation.utils.percentEncodeBaggageValue

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
        if (!isValidBaggageKey(name)) {
            return null
        }
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
        sb.append(key).append(KEY_VALUE_DELIMITER).append(percentEncodeBaggageValue(value))
        if (metadata.isNotEmpty()) {
            val encodedMetadata = encodeMetadata(metadata)
            if (encodedMetadata != null) {
                sb.append(METADATA_DELIMITER).append(encodedMetadata)
            }
        }
        return sb.toString()
    }

    /**
     * Validates and encodes metadata according to W3C property grammar.
     * Returns null if metadata is invalid, causing the entry to be serialized without metadata.
     * Property format: key OWS "=" OWS value OR key OWS
     * Both key and value must be baggage-octet (ASCII subset, no CTLs, whitespace, comma, semicolon, etc.)
     */
    private fun encodeMetadata(metadata: String): String? {
        val sb = StringBuilder()
        var pos = 0
        while (pos < metadata.length) {
            // Skip leading whitespace
            while (pos < metadata.length && metadata[pos] == SPACE || metadata[pos] == HTAB) {
                pos++
            }
            if (pos >= metadata.length) break

            // Parse property key
            val keyStart = pos
            while (pos < metadata.length && isBaggageOctet(metadata[pos]) && metadata[pos] != '=') {
                pos++
            }
            val key = metadata.substring(keyStart, pos)

            // Key must be a valid token (non-empty and valid baggage-octet)
            if (key.isEmpty() || !isValidToken(key)) {
                return null
            }

            // Check for value separator
            val hasValue = pos < metadata.length && metadata[pos] == '='
            if (hasValue) {
                pos++ // skip '='
                // Skip whitespace after '='
                while (pos < metadata.length && (metadata[pos] == SPACE || metadata[pos] == HTAB)) {
                    pos++
                }
                // Parse property value
                val valueStart = pos
                while (pos < metadata.length && isBaggageOctet(metadata[pos]) && metadata[pos] != ';') {
                    pos++
                }
                val value = metadata.substring(valueStart, pos)
                // Percent-encode the value
                sb.append(key).append('=').append(percentEncodeProperty(value))
            } else {
                // Property without value
                sb.append(key)
            }

            // Skip trailing whitespace
            while (pos < metadata.length && (metadata[pos] == SPACE || metadata[pos] == HTAB)) {
                pos++
            }

            // Check for next property or end
            if (pos < metadata.length && metadata[pos] == ';') {
                pos++
                sb.append(';')
            } else if (pos < metadata.length) {
                // Invalid character or separator
                return null
            }
        }
        return sb.takeIf(StringBuilder::isNotEmpty)?.toString()
    }

    /**
     * Checks if a character is a valid baggage-octet per W3C spec.
     * baggage-octet = %x21 / %x23-2B / %x2D-3A / %x3C-5B / %x5D-7E
     * Excludes CTLs, whitespace, DQUOTE, comma, semicolon, and backslash
     */
    private fun isBaggageOctet(c: Char): Boolean = when (c.code) {
        0x21 -> true // !
        in 0x23..0x2B -> true // # - +
        in 0x2D..0x3A -> true // - - :
        in 0x3C..0x5B -> true // < - [
        in 0x5D..0x7E -> true // ] - ~
        else -> false
    }

    /**
     * Checks if a string is a valid token per RFC 7230.
     * Uses the same validation as isValidBaggageKey from BaggageValidation.kt
     */
    private fun isValidToken(name: String): Boolean {
        if (name.isEmpty()) {
            return false
        }
        return name.all { c ->
            (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') ||
            c in setOf('!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|', '~')
        }
    }

    /**
     * Percent-encodes property values that contain non-baggage-octet characters.
     * Similar to percentEncodeBaggageValue but specifically for property values.
     */
    private fun percentEncodeProperty(value: String): String {
        val sb = StringBuilder()
        for (c in value) {
            if (isBaggageOctet(c)) {
                sb.append(c)
            } else {
                // Percent-encode the character as UTF-8 bytes
                val bytes = c.toString().encodeToByteArray()
                for (b in bytes) {
                    sb.append(String.format("%%%02X", b))
                }
            }
        }
        return sb.toString()
    }

    private fun decode(header: String): Baggage? {
        val budget = HeaderBudget(MAX_HEADER_BYTES)
        val baggage = createBaggage {
            for (rawElement in header.split(ENTRY_DELIMITER)) {
                val parsed = parseElementIfFits(rawElement, budget) ?: continue
                put(parsed.key, parsed.value, parsed.metadata)
            }
        }
        return baggage.takeIf { it.asMap().isNotEmpty() }
    }

    /**
     * Trims and decodes [rawElement], honoring the per-entry size limit and [budget] for the
     * whole header. Returns `null` for elements that are empty, malformed, or too large.
     * Malformed entries do not consume any of the header's budget, but an oversized element
     * permanently exhausts [budget] so later, smaller elements are skipped too -- matching how a
     * real client would stop appending once the header is full.
     */
    private fun parseElementIfFits(rawElement: String, budget: HeaderBudget): ParsedEntry? {
        val element = rawElement.trim(SPACE, HTAB)
        if (element.isEmpty() || element.length > MAX_ENTRY_BYTES) {
            return null
        }
        if (!budget.hasRoomFor(element.length)) {
            budget.exhaust()
            return null
        }
        val parsed = decodeEntry(element) ?: return null
        budget.reserve(element.length)
        return parsed
    }

    /** Tracks how many bytes of [MAX_HEADER_BYTES] remain as baggage entries are accumulated. */
    private class HeaderBudget(private var remainingBytes: Int) {
        private var hasEntries = false

        private val separatorBytes: Int
            get() = when {
                hasEntries -> 1
                else -> 0
            }

        /** Whether an entry of [entryLength] bytes, plus its delimiter, still fits. */
        fun hasRoomFor(entryLength: Int): Boolean = separatorBytes + entryLength <= remainingBytes

        /** Commits the space for an entry already confirmed to fit via [hasRoomFor]. */
        fun reserve(entryLength: Int) {
            remainingBytes -= separatorBytes + entryLength
            hasEntries = true
        }

        /** Permanently blocks any further entries from fitting. */
        fun exhaust() {
            remainingBytes = 0
        }
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
        if (!isValidBaggageKey(key)) {
            return null
        }
        val rawValue = keyValuePart.substring(eq + 1).trim(SPACE, HTAB)
        val value = percentDecodeBaggageValue(rawValue) ?: return null
        return ParsedEntry(key, value, metadata.trim(SPACE, HTAB))
    }

    private class ParsedEntry(val key: String, val value: String, val metadata: String)
}
