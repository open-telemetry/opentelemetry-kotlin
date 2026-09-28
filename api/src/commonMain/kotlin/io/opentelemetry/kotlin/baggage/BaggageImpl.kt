package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class BaggageImpl private constructor(
    private val entries: Map<String, BaggageEntry>,
) : Baggage {

    override fun getValue(name: String): String? = entries[name]?.value

    override fun asMap(): Map<String, BaggageEntry> = entries

    override fun set(name: String, value: String): Baggage =
        setImpl(name, value, EMPTY_METADATA)

    override fun set(name: String, value: String, metadata: BaggageEntryMetadata): Baggage =
        setImpl(name, value, metadata)

    override fun remove(name: String): Baggage =
        when (name) {
            !in entries -> this
            else -> BaggageImpl(entries - name)
        }

    private fun setImpl(name: String, value: String, metadata: BaggageEntryMetadata): Baggage {
        if (!isValidKey(name)) {
            return this
        }
        if (!isValidValue(value)) {
            return this
        }
        if (entries.size >= MAX_ENTRIES && name !in entries) {
            return this
        }
        return BaggageImpl(entries + (name to BaggageEntryImpl(value, metadata)))
    }

    companion object {
        const val MAX_ENTRIES = 180

        val EMPTY: Baggage = BaggageImpl(emptyMap())

        private val EMPTY_METADATA = BaggageEntryMetadataImpl("")

        private val TCHAR_SPECIALS = setOf(
            '!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|', '~',
        )

        /**
         * RFC 7230 token: 1*tchar.
         * tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." / "^" / "_" / "`" / "|" / "~" / DIGIT / ALPHA
         */
        private fun isValidKey(name: String): Boolean {
            if (name.isEmpty()) {
                return false
            }
            return name.all(::isTChar)
        }

        /**
         * Reject characters that would break the W3C wire format outright (CR, LF) or are
         * meaningless inside a baggage value (NUL). Other non-octet characters are accepted
         * and percent-encoded by the propagator at inject time.
         */
        private fun isValidValue(value: String): Boolean =
            value.all { c -> c != '\r' && c != '\n' && c.code != 0 }

        private fun isTChar(c: Char): Boolean {
            if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') {
                return true
            }
            return c in TCHAR_SPECIALS
        }
    }
}
