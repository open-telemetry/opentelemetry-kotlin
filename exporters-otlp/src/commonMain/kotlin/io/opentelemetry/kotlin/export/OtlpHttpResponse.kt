package io.opentelemetry.kotlin.export

/**
 * A transport-independent view of an OTLP/HTTP response.
 *
 * [retryAfterMs] is the Retry-After delay normalized by the transport for retryable responses, or
 * null when the response is not retryable, the header is absent, or the header is malformed.
 * [body] is capped by the transport at [MAX_RESPONSE_BODY_BYTES] + 1 bytes so that a hostile server
 * cannot exhaust memory; [OtlpResponseProcessor] rejects anything larger.
 */
internal class OtlpHttpResponse(
    val statusCode: Int,
    val retryAfterMs: Long?,
    val body: ByteArray,
)

internal const val MAX_RESPONSE_BODY_BYTES: Long = 4 * 1024 * 1024
