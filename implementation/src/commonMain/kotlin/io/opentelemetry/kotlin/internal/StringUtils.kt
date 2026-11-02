package io.opentelemetry.kotlin.internal

/**
 * Counts Unicode code points in this string, treating each surrogate pair as a single code
 * point. `String.length` is not used directly because on all Kotlin targets it counts UTF-16
 * code units, which over-counts characters outside the Basic Multilingual Plane.
 */
internal fun String.codePointCount(): Int {
    var count = 0
    var index = 0
    while (index < length) {
        index += if (isHighSurrogateAt(index)) {
            2
        } else {
            1
        }
        count++
    }
    return count
}

/**
 * Returns the prefix of this string containing at most [maxCodePoints] Unicode code points,
 * never splitting a surrogate pair.
 */
internal fun String.takeCodePoints(maxCodePoints: Int): String {
    var count = 0
    var index = 0
    while (index < length && count < maxCodePoints) {
        index += if (isHighSurrogateAt(index)) {
            2
        } else {
            1
        }
        count++
    }
    return substring(0, index)
}

private fun String.isHighSurrogateAt(index: Int): Boolean {
    val char = this[index]
    return char.isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()
}
