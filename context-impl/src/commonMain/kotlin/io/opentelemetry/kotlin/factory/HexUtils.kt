package io.opentelemetry.kotlin.factory

/**
 * Returns true if the character is a valid hexadecimal digit (0-9, a-f, A-F).
 */
public fun Char.isHexDigit(): Boolean {
    return this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
}

/**
 * Returns true if the character is a valid lowercase hexadecimal digit (0-9, a-f).
 */
public fun Char.isLowercaseHexDigit(): Boolean {
    return this in '0'..'9' || this in 'a'..'f'
}

/**
 * Returns true if the string contains only valid hexadecimal characters.
 */
public fun String.isValidHex(): Boolean {
    return this.all { it.isHexDigit() }
}

/**
 * Returns true if the string contains only valid lowercase hexadecimal characters.
 */
public fun String.isValidLowercaseHex(): Boolean {
    return this.all { it.isLowercaseHexDigit() }
}

/**
 * Returns true if the hex-encoded value represents zero.
 */
public fun String.isAllZerosHex(): Boolean = all { it == '0' }
