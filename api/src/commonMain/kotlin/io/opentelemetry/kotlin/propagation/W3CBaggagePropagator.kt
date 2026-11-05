package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageEntry
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.context.Context
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
    private const val MAX_MEMBERS = 64

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
        var memberCount = 0
        for ((name, entry) in entries) {
            if (memberCount >= MAX_MEMBERS) {
                break
            }
            val piece = encodeIfFits(name, entry, builder.utf8ByteLength()) ?: continue
            if (builder.isNotEmpty()) {
                builder.append(ENTRY_DELIMITER)
            }
            builder.append(piece)
            memberCount++
        }
        return builder.takeIf(StringBuilder::isNotEmpty)?.toString()
    }

    private fun encodeIfFits(name: String, entry: BaggageEntry, currentByteLength: Int): String? {
        val piece = encodeEntry(name, entry.value, entry.metadata.value)
        val pieceByteLength = piece.utf8ByteLength()
        val separator = when (currentByteLength) {
            0 -> 0
            else -> 1
        }
        if (currentByteLength + separator + pieceByteLength > MAX_HEADER_BYTES) {
            return null
        }
        return piece
    }

    private fun encodeEntry(key: String, value: String, metadata: String): String {
        val sb = StringBuilder()
        sb.append(key).append(KEY_VALUE_DELIMITER).append(percentEncodeBaggageValue(value))
        if (metadata.isNotEmpty()) {
            sb.append(METADATA_DELIMITER).append(metadata)
        }
        return sb.toString()
    }

    private fun decode(header: String): Baggage? {
        val budget = HeaderBudget(MAX_HEADER_BYTES)
        val baggage = createBaggage {
            var memberCount = 0
            for (rawElement in header.split(ENTRY_DELIMITER)) {
                if (memberCount >= MAX_MEMBERS) {
                    break
                }
                val parsed = parseElementIfFits(rawElement, budget) ?: continue
                put(parsed.key, parsed.value, parsed.metadata)
                memberCount++
            }
        }
        return baggage.takeIf { it.asMap().isNotEmpty() }
    }

    /**
     * Trims and decodes [rawElement], honoring the [budget] for the whole header.
     * Returns `null` for elements that are empty or malformed.
     * An oversized element permanently exhausts [budget] so later, smaller elements are skipped too
     * -- matching how a real client would stop appending once the header is full.
     */
    private fun parseElementIfFits(rawElement: String, budget: HeaderBudget): ParsedEntry? {
        val element = rawElement.trim(SPACE, HTAB)
        if (element.isEmpty()) {
            return null
        }
        val elementByteLength = element.utf8ByteLength()
        if (!budget.hasRoomFor(elementByteLength)) {
            budget.exhaust()
            return null
        }
        val parsed = decodeEntry(element) ?: return null
        budget.reserve(elementByteLength)
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
        val rawValue = keyValuePart.substring(eq + 1).trim(SPACE, HTAB)
        val value = percentDecodeBaggageValue(rawValue) ?: return null
        return ParsedEntry(key, value, metadata.trim(SPACE, HTAB))
    }

    private class ParsedEntry(val key: String, val value: String, val metadata: String)

    /** Returns the UTF-8 byte length of this string. */
    private fun String.utf8ByteLength(): Int = encodeToByteArray().size

    /** Returns the UTF-8 byte length of this StringBuilder's current content. */
    private fun StringBuilder.utf8ByteLength(): Int = toString().utf8ByteLength()
}
