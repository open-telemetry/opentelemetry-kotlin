package io.opentelemetry.kotlin.propagation.utils

private val TCHAR_SPECIALS = setOf(
    '!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|', '~',
)

/**
 * RFC 7230 token: 1*tchar.
 * tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." / "^" / "_" / "`" / "|" / "~" / DIGIT / ALPHA
 */
public fun isValidBaggageKey(name: String): Boolean {
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
public fun isValidBaggageValue(value: String): Boolean =
    value.all { c -> c != '\r' && c != '\n' && c.code != 0 }

private fun isTChar(c: Char): Boolean {
    if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') {
        return true
    }
    return c in TCHAR_SPECIALS
}
