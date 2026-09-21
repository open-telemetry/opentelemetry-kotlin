package io.opentelemetry.kotlin.propagation

/**
 * Codec for the list format used by the W3C `tracestate` header.
 *
 * https://www.w3.org/TR/trace-context-2/#tracestate-header
 *
 * This handles the structure of the list: splitting on the list-member separator, trimming
 * optional whitespace, locating the key/value separator, and applying the spec size limits.
 * Validating the characters of a key or value is the responsibility of the `TraceState`
 * implementation.
 */
public object W3CTraceStateCodec {

    private const val LIST_MEMBER_SEPARATOR = ','
    private const val KEY_VALUE_SEPARATOR = '='
    private const val OWS_SPACE = ' '
    private const val OWS_HTAB = '\t'

    /** https://www.w3.org/TR/trace-context-2/#tracestate-header-field-values */
    private const val MAX_LIST_MEMBERS = 32

    /** https://www.w3.org/TR/trace-context-2/#tracestate-limits */
    private const val MAX_HEADER_CHARS = 512
    private const val LARGE_MEMBER_CHARS = 128

    /**
     * Encodes [entries] as a `tracestate` header value, preserving iteration order.
     *
     * At most 32 members are written. If the combined header would exceed 512 characters,
     * whole members longer than 128 characters are dropped first, then members from the end,
     * until the result fits.
     */
    public fun encode(entries: Map<String, String>): String = join(sanitize(entries))

    /**
     * Decodes a `tracestate` header value into its entries, preserving the order in which they
     * appear in [header].
     *
     * Malformed list members are skipped rather than failing the whole header, and where a key is
     * repeated the first occurrence wins. At most 32 members are kept; combined length is then
     * truncated using the same rules as [encode]. This never throws.
     */
    public fun decode(header: String): Map<String, String> {
        val entries = LinkedHashMap<String, String>()
        header.split(LIST_MEMBER_SEPARATOR).forEach { raw ->
            val member = raw.trim(OWS_SPACE, OWS_HTAB)
            val eq = member.indexOf(KEY_VALUE_SEPARATOR)
            if (eq <= 0 || eq == member.length - 1) {
                // skip empty/invalid members
                return@forEach
            }
            val key = member.substring(0, eq)
            if (!entries.containsKey(key) && entries.size < MAX_LIST_MEMBERS) {
                entries[key] = member.substring(eq + 1)
            }
        }
        truncateToCharLimit(entries)
        return entries
    }

    private fun sanitize(entries: Map<String, String>): Map<String, String> {
        if (entries.size <= MAX_LIST_MEMBERS && encodedChars(entries) <= MAX_HEADER_CHARS) {
            return entries
        }
        val result = LinkedHashMap<String, String>()
        for ((key, value) in entries) {
            if (result.size >= MAX_LIST_MEMBERS) {
                break
            }
            result[key] = value
        }
        truncateToCharLimit(result)
        return result
    }

    /**
     * Drops whole list-members until the encoded header is at most [MAX_HEADER_CHARS].
     * Members longer than [LARGE_MEMBER_CHARS] are removed first (from the end), then members
     * from the end.
     */
    private fun truncateToCharLimit(entries: MutableMap<String, String>) {
        while (entries.isNotEmpty() && encodedChars(entries) > MAX_HEADER_CHARS) {
            val largeKey = entries.entries.findLast { (key, value) ->
                memberChars(key, value) > LARGE_MEMBER_CHARS
            }?.key
            if (largeKey != null) {
                entries.remove(largeKey)
            } else {
                entries.remove(entries.keys.last())
            }
        }
    }

    private fun join(entries: Map<String, String>): String = buildString {
        entries.forEach {
            if (isNotEmpty()) {
                append(LIST_MEMBER_SEPARATOR)
            }
            append(it.key)
            append(KEY_VALUE_SEPARATOR)
            append(it.value)
        }
    }

    private fun memberChars(key: String, value: String): Int = key.length + 1 + value.length

    private fun encodedChars(entries: Map<String, String>): Int {
        if (entries.isEmpty()) {
            return 0
        }
        var chars = entries.size - 1
        entries.forEach { (key, value) ->
            chars += memberChars(key, value)
        }
        return chars
    }
}
