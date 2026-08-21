package io.opentelemetry.kotlin.tracing.sampling

/** Length in bytes of a trace ID. */
private const val TRACE_ID_BYTES = 16

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

/**
 * Validates that [ratio] is a usable sampling probability, i.e. in `[2^-56, 1]`.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/tracestate-probability-sampling/#sampling-probability
 */
internal fun validateRatio(ratio: Double) {
    require(ratio in (1.0 / MAX_THRESHOLD)..1.0) { "ratio must be between 2^-56 and 1, got $ratio" }
}

/**
 * Converts a sampling probability [ratio] into its equivalent rejection threshold (T).
 *
 * https://opentelemetry.io/docs/specs/otel/trace/tracestate-probability-sampling/#rejection-threshold-t
 * https://opentelemetry.io/docs/specs/otel/trace/tracestate-probability-sampling/#converting-floating-point-probability-to-threshold-value
 */
internal fun thresholdFromRatio(ratio: Double) = MAX_THRESHOLD - (ratio * MAX_THRESHOLD).toLong()
