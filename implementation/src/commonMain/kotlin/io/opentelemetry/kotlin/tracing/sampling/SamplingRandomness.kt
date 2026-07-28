package io.opentelemetry.kotlin.tracing.sampling

/**
 * Derives the 56-bit randomness value (R) from the least-significant 7 bytes of a hex-encoded
 * trace ID, per W3C Trace Context Level 2.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/tracestate-probability-sampling/#randomness-value-r
 * https://www.w3.org/TR/trace-context-2/#randomness-of-trace-id
 */
internal fun randomnessFromTraceId(traceId: String): Long =
    (byteFromBase16(traceId[18], traceId[19]) shl 48) or
        (byteFromBase16(traceId[20], traceId[21]) shl 40) or
        (byteFromBase16(traceId[22], traceId[23]) shl 32) or
        (byteFromBase16(traceId[24], traceId[25]) shl 24) or
        (byteFromBase16(traceId[26], traceId[27]) shl 16) or
        (byteFromBase16(traceId[28], traceId[29]) shl 8) or
        byteFromBase16(traceId[30], traceId[31])

/** Parses a single byte (two hex characters) into its numeric value. */
internal fun byteFromBase16(first: Char, second: Char): Long =
    ((first.digitToInt(16) shl 4) or second.digitToInt(16)).toLong()

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
