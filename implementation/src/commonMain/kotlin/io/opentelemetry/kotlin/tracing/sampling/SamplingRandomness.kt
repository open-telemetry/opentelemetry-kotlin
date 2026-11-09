package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.propagation.utils.TRACE_ID_BYTES
import kotlin.math.roundToLong

/** Index of the first of the 7 least-significant bytes that supply the 56-bit randomness value. */
private const val RANDOMNESS_OFFSET = 9

/**
 * Derives the 56-bit randomness value (R) from the least-significant 7 bytes of a trace ID,
 * per W3C Trace Context Level 2.
 *
 * Returns `0` if [traceIdBytes] is not a well-formed trace ID. A third-party SpanContext
 * implementation can supply a malformed trace ID, and sampling must not throw in that case.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/tracestate-probability-sampling/#randomness-value-r
 * https://www.w3.org/TR/trace-context-2/#randomness-of-trace-id
 */
internal fun randomnessFromTraceIdBytes(traceIdBytes: ByteArray): Long {
    if (traceIdBytes.size != TRACE_ID_BYTES) {
        return 0L
    }
    var result = 0L
    for (i in RANDOMNESS_OFFSET until TRACE_ID_BYTES) {
        result = (result shl 8) or (traceIdBytes[i].toLong() and 0xFF)
    }
    return result
}

private const val MAX_THRESHOLD: Long = 1L shl 56
private const val THRESHOLD_HEX_DIGITS = 14
private const val BITS_PER_HEX_DIGIT = 4
private const val DEFAULT_THRESHOLD_PRECISION = 4

/** The smallest sampling probability that can be represented as a threshold, `2^-56`. */
internal const val MIN_SAMPLING_RATIO: Double = 1.0 / MAX_THRESHOLD

/**
 * Validates that [ratio] is a usable sampling probability, i.e. in `[2^-56, 1]`.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/tracestate-probability-sampling/#sampling-probability
 */
internal fun validateRatio(ratio: Double) {
    require(ratio in MIN_SAMPLING_RATIO..1.0) { "ratio must be between 2^-56 and 1, got $ratio" }
}

/**
 * Converts a sampling probability [ratio] into its equivalent rejection threshold (T).
 * Rounds to [precision] significant hexadecimal digits, retaining additional digits for
 * small probabilities so that leading `f` digits do not consume the requested precision.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/tracestate-probability-sampling/#rejection-threshold-t
 * https://opentelemetry.io/docs/specs/otel/trace/tracestate-probability-sampling/#converting-floating-point-probability-to-threshold-value
 */
internal fun thresholdFromRatio(ratio: Double, precision: Int = DEFAULT_THRESHOLD_PRECISION): Long {
    require(precision in 1..THRESHOLD_HEX_DIGITS) { "precision must be between 1 and 14, got $precision" }
    val sampledCount = (ratio * MAX_THRESHOLD).roundToLong()
    val threshold = MAX_THRESHOLD - sampledCount
    val unusedBits = Long.SIZE_BITS - THRESHOLD_HEX_DIGITS * BITS_PER_HEX_DIGIT
    val leadingDigits = ((sampledCount.countLeadingZeroBits() - unusedBits) / BITS_PER_HEX_DIGIT).coerceAtLeast(0)
    val discardedBits = (THRESHOLD_HEX_DIGITS - precision - leadingDigits).coerceAtLeast(0) * BITS_PER_HEX_DIGIT
    if (discardedBits == 0) {
        return threshold
    }
    val half = 1L shl (discardedBits - 1)
    return ((threshold + half) ushr discardedBits) shl discardedBits
}
