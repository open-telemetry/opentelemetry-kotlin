package io.opentelemetry.kotlin.propagation.utils

/** Lowercase hex digits, indexed by nibble value. */
private val HEX_DIGITS = "0123456789abcdef".toCharArray()

/** ASCII code point -> nibble value, or -1 if the character is not a hex digit. */
private val HEX_VALUES = IntArray(128) { -1 }.apply {
    for (i in 0..9) {
        this['0'.code + i] = i
    }
    for (i in 0..5) {
        this['a'.code + i] = 10 + i
        this['A'.code + i] = 10 + i
    }
}

/**
 * Encodes the bytes as a lowercase hex string.
 */
public fun ByteArray.encodeHex(): String {
    val chars = CharArray(size * 2)
    var index = 0
    for (b in this) {
        val i = b.toInt() and 0xFF
        chars[index++] = HEX_DIGITS[i shr 4]
        chars[index++] = HEX_DIGITS[i and 0x0F]
    }
    return chars.concatToString()
}

/**
 * Decodes a hex string (case-insensitive) into bytes. A string that is not valid hex, or that has an
 * odd length, returns an empty [ByteArray].
 */
public fun String.decodeHexOrEmpty(): ByteArray {
    if (length % 2 != 0) {
        return ByteArray(0)
    }
    val out = ByteArray(length / 2)
    for (i in out.indices) {
        val hi = this[i * 2].hexDigitValue()
        val lo = this[i * 2 + 1].hexDigitValue()
        if (hi < 0 || lo < 0) {
            return ByteArray(0)
        }
        out[i] = ((hi shl 4) or lo).toByte()
    }
    return out
}

/**
 * Returns the nibble value of this character if it is an ASCII hex digit (case-insensitive), or -1 otherwise.
 */
public fun Char.hexDigitValue(): Int {
    if (code >= HEX_VALUES.size) {
        return -1
    }
    return HEX_VALUES[code]
}
