package io.opentelemetry.kotlin.propagation

/**
 * Codec for the list format used by the W3C `tracestate` header.
 *
 * https://www.w3.org/TR/trace-context-2/#tracestate-header
 *
 * This handles the structure of the list only: splitting on the list-member separator, trimming
 * optional whitespace, and locating the key/value separator. Validating the characters of a key or
 * value, and capping the number of entries, is the responsibility of the `TraceState`
 * implementation.
 */
public object W3CTraceStateCodec {

    private const val LIST_MEMBER_SEPARATOR = ','
    private const val KEY_VALUE_SEPARATOR = '='
    private const val OWS_SPACE = ' '
    private const val OWS_HTAB = '\t'

    /**
     * Encodes [entries] as a `tracestate` header value, preserving iteration order.
     */
    public fun encode(entries: Map<String, String>): String = buildString {
        entries.forEach {
            if (isNotEmpty()) {
                append(LIST_MEMBER_SEPARATOR)
            }
            append(it.key)
            append(KEY_VALUE_SEPARATOR)
            append(it.value)
        }
    }

    /**
     * Decodes a `tracestate` header value into its entries, preserving the order in which they
     * appear in [header].
     *
     * Malformed list members are skipped rather than failing the whole header, and where a key is
     * repeated the first occurrence wins. This never throws.
     */
    public fun decode(header: String): Map<String, String> {
        val entries = mutableMapOf<String, String>()
        header.split(LIST_MEMBER_SEPARATOR).forEach { raw ->
            val member = raw.trim(OWS_SPACE, OWS_HTAB)
            val eq = member.indexOf(KEY_VALUE_SEPARATOR)
            if (eq <= 0 || eq == member.length - 1) {
                // skip empty/invalid members
                return@forEach
            }
            val key = member.substring(0, eq)
            if (!entries.containsKey(key)) {
                entries[key] = member.substring(eq + 1)
            }
        }
        return entries
    }
}
