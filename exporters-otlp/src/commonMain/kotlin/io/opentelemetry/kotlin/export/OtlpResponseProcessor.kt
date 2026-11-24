package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportUserCodeError

internal fun isRetryableStatus(statusCode: Int): Boolean =
    statusCode == 429 || statusCode == 502 || statusCode == 503 || statusCode == 504

/**
 * Classifies a transport-independent [OtlpHttpResponse] according to the OTLP/HTTP spec:
 * https://opentelemetry.io/docs/specs/otlp/#otlphttp-response
 *
 * This class has no dependency on the HTTP implementation so it can move to a
 * transport-independent module.
 */
internal class OtlpResponseProcessor(
    private val sdkErrorHandler: SdkErrorHandler,
) {

    fun classify(
        response: OtlpHttpResponse,
        parsePartialSuccess: (body: ByteArray) -> OtlpPartialSuccess?,
    ): OtlpResponse {
        val code = response.statusCode
        val body = response.body
        if (body.size > MAX_RESPONSE_BODY_BYTES) {
            sdkErrorHandler.reportUserCodeError(
                IllegalStateException("OTLP response body exceeded $MAX_RESPONSE_BODY_BYTES bytes"),
                "OTLP response discarded (status=$code)",
            )
            return OtlpResponse.ResponseTooLarge(code)
        }
        return when {
            code in 200..299 -> when (val partialSuccess = parsePartialSuccess(body)) {
                null -> OtlpResponse.Success
                else -> OtlpResponse.PartialSuccess(partialSuccess.rejectedCount, partialSuccess.errorMessage)
            }

            isRetryableStatus(code) -> OtlpResponse.RetryableError(
                code,
                response.retryAfterMs,
                body.deserializeStatusMessage(),
            )

            code in 400..499 -> OtlpResponse.ClientError(code, body.deserializeStatusMessage())
            code in 500..599 -> OtlpResponse.ServerError(code, body.deserializeStatusMessage())
            else -> OtlpResponse.UnexpectedStatus(code)
        }
    }
}
