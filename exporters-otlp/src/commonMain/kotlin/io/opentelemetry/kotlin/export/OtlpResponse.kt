package io.opentelemetry.kotlin.export

/**
 * The outcome of an OTLP/HTTP export request, classified as described in
 * https://opentelemetry.io/docs/specs/otlp/#otlphttp-response
 */
internal sealed class OtlpResponse(val statusCode: Int) {

    /**
     * The server accepted all telemetry.
     */
    object Success : OtlpResponse(200) {
        override fun toString(): String {
            return "Success(statusCode=$statusCode)"
        }
    }

    /**
     * The server accepted the request but populated `partial_success`. This MUST NOT be retried.
     */
    class PartialSuccess(
        val rejectedCount: Long,
        val errorMessage: String?,
    ) : OtlpResponse(200) {
        override fun toString(): String {
            return "PartialSuccess(rejectedCount=$rejectedCount, errorMessage=$errorMessage, statusCode=$statusCode)"
        }
    }

    /**
     * A failure that MUST NOT be retried.
     */
    sealed class UnretryableError(statusCode: Int) : OtlpResponse(statusCode)

    /**
     * A 4xx response that is not retryable.
     */
    class ClientError(statusCode: Int, val errorMessage: String?) : UnretryableError(statusCode) {
        override fun toString(): String {
            return "ClientError(errorMessage=$errorMessage, statusCode=$statusCode)"
        }
    }

    /**
     * A 5xx response that is not retryable.
     */
    class ServerError(statusCode: Int, val errorMessage: String?) : UnretryableError(statusCode) {
        override fun toString(): String {
            return "ServerError(errorMessage=$errorMessage, statusCode=$statusCode)"
        }
    }

    /**
     * A 429, 502, 503 or 504 response, which SHOULD be retried after [retryAfterMs] if the server
     * supplied a Retry-After header.
     */
    class RetryableError(
        statusCode: Int,
        val retryAfterMs: Long?,
        val errorMessage: String?,
    ) : OtlpResponse(statusCode) {
        override fun toString(): String {
            return "RetryableError(errorMessage=$errorMessage, retryAfterMs=$retryAfterMs, statusCode=$statusCode)"
        }
    }

    /**
     * The server responded with a status code that OTLP does not define (e.g. an unfollowed 3xx).
     * This is treated as a non-retryable failure according to HTTP semantics.
     */
    class UnexpectedStatus(statusCode: Int) : UnretryableError(statusCode) {
        override fun toString(): String {
            return "UnexpectedStatus(statusCode=$statusCode)"
        }
    }

    /**
     * The response body exceeded the client's size limit, so it was discarded. This MUST be treated
     * as a non-retryable error.
     */
    class ResponseTooLarge(statusCode: Int) : UnretryableError(statusCode) {
        override fun toString(): String {
            return "ResponseTooLarge(statusCode=$statusCode)"
        }
    }

    /**
     * No HTTP response was received (e.g. a network failure or timeout). This SHOULD be retried.
     */
    object Unknown : OtlpResponse(-1) {
        override fun toString(): String {
            return "Unknown(statusCode=$statusCode)"
        }
    }
}
