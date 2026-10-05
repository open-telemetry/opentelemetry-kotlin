package io.opentelemetry.kotlin.propagation.utils

private const val PERCENT_CHAR = '%'
private const val PERCENT = 0x25
private const val BYTE_MASK = 0xFF
private const val HEX_SHIFT = 4
private const val HEX_MASK = 0xF
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

private val HEX_UPPER = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
)

/**
 * Percent-encode bytes outside `baggage-octet`. The `%` byte is also encoded so the
 * decoder can unambiguously distinguish literals from `%HH` sequences.
 *
 * baggage-octet = %x21 / %x23-2B / %x2D-3A / %x3C-5B / %x5D-7E
 */
public fun percentEncodeBaggageValue(value: String): String {
    val sb = StringBuilder(value.length)
    for (byte in value.encodeToByteArray()) {
        val b = byte.toInt() and BYTE_MASK
        if (b == PERCENT || !isBaggageOctet(b)) {
            sb.append(PERCENT_CHAR)
            sb.append(HEX_UPPER[b ushr HEX_SHIFT])
            sb.append(HEX_UPPER[b and HEX_MASK])
        } else {
            sb.append(b.toChar())
        }
    }
    return sb.toString()
}

/**
 * Decodes `%HH` sequences in [value]. Returns null if [value] contains a malformed sequence.
 */
public fun percentDecodeBaggageValue(value: String): String? {
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
    val hi = value[start + 1].hexDigitValue()
    val lo = value[start + 2].hexDigitValue()
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
