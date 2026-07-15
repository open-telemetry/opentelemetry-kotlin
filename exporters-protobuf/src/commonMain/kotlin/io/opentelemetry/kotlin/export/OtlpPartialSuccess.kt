package io.opentelemetry.kotlin.export

/**
 * The parsed `partial_success` field of an OTLP export response.
 *
 * Per the OTLP spec a `partial_success` message with an empty value (rejected count of `0` and an
 * empty `error_message`) is equivalent to it being absent, so this type is only produced when the
 * server actually rejected some telemetry or supplied a warning message.
 */
public data class OtlpPartialSuccess(
    val rejectedCount: Long,
    val errorMessage: String,
)
