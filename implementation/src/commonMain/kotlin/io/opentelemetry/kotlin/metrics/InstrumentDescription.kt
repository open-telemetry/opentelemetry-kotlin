package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

/**
 * The minimum number of Unicode code points instrument descriptions must support, per
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-description.
 */
internal const val MAX_INSTRUMENT_DESCRIPTION_CHARS = 1023

/**
 * Sanitizes [description] to satisfy the
 * [instrument description](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-description)
 * requirements: an opaque string that MUST support at least [MAX_INSTRUMENT_DESCRIPTION_CHARS]
 * Unicode code points. Descriptions exceeding that length are truncated and reported as
 * [SdkError.ApiMisuse] so the host is never destabilized by invalid input.
 *
 * `null` and strings within the limit are returned unchanged.
 */
internal fun SdkErrorHandler.sanitizeInstrumentDescription(description: String?): String? {
    if (description == null) {
        return null
    }
    val codePointCount = description.codePointCount()
    if (codePointCount <= MAX_INSTRUMENT_DESCRIPTION_CHARS) {
        return description
    }
    reportError(
        SdkError.ApiMisuse(
            api = "Instrument.description",
            message = "Instrument description has $codePointCount code points, exceeding the " +
                "$MAX_INSTRUMENT_DESCRIPTION_CHARS code point limit; it was truncated.",
            severity = SdkErrorSeverity.WARNING,
        )
    )
    return description.takeCodePoints(MAX_INSTRUMENT_DESCRIPTION_CHARS)
}

/**
 * Counts Unicode code points in this string, treating each surrogate pair as a single code
 * point. `String.length` is not used directly because on all Kotlin targets it counts UTF-16
 * code units, which over-counts characters outside the Basic Multilingual Plane.
 */
private fun String.codePointCount(): Int {
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
private fun String.takeCodePoints(maxCodePoints: Int): String {
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
