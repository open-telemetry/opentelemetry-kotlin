package io.opentelemetry.kotlin.export

import io.ktor.client.HttpClient
import io.ktor.client.plugins.compression.compress
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.readRemaining
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import io.opentelemetry.kotlin.error.reportUserCodeError
import io.opentelemetry.kotlin.export.OtlpClient.Companion.MAX_RESPONSE_BODY_BYTES
import io.opentelemetry.kotlin.export.OtlpResponse.ClientError
import io.opentelemetry.kotlin.export.OtlpResponse.PartialSuccess
import io.opentelemetry.kotlin.export.OtlpResponse.ResponseTooLarge
import io.opentelemetry.kotlin.export.OtlpResponse.RetryableError
import io.opentelemetry.kotlin.export.OtlpResponse.ServerError
import io.opentelemetry.kotlin.export.OtlpResponse.Success
import io.opentelemetry.kotlin.export.OtlpResponse.UnexpectedStatus
import io.opentelemetry.kotlin.export.OtlpResponse.Unknown
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.export.deserializeLogRecordPartialSuccess
import io.opentelemetry.kotlin.logging.export.toProtobufByteArray
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.deserializeTraceRecordPartialSuccess
import io.opentelemetry.kotlin.tracing.export.toProtobufByteArray
import kotlinx.io.readByteArray

internal class OtlpClient(
    val baseUrl: String,
    private val httpClient: HttpClient,
    private val sdkErrorHandler: SdkErrorHandler,
    private val headers: suspend () -> Map<String, String> = { emptyMap() },
) {

    private val contentType = ContentType.parse("application/x-protobuf")
    private val userAgent = "OTel-OTLP-Exporter-Kotlin/${BuildKonfig.VERSION}"

    suspend fun exportLogs(telemetry: List<LogRecordData>): OtlpResponse = exportTelemetry(
        OtlpEndpoint.Logs,
        telemetry::toProtobufByteArray,
        ByteArray::deserializeLogRecordPartialSuccess
    )

    suspend fun exportTraces(telemetry: List<SpanData>): OtlpResponse = exportTelemetry(
        OtlpEndpoint.Traces,
        telemetry::toProtobufByteArray,
        ByteArray::deserializeTraceRecordPartialSuccess
    )

    private suspend fun exportTelemetry(
        endpoint: OtlpEndpoint,
        requestSerializer: () -> ByteArray,
        parsePartialSuccess: (body: ByteArray) -> OtlpPartialSuccess?,
    ): OtlpResponse = sdkErrorHandler.guardOrDefaultSuspend(Unknown, "OTLP export failed") {
        val url = "$baseUrl/${endpoint.path}"
        val requestHeaders = headers()
        val response = httpClient.post(url) {
            compress("gzip")
            contentType(contentType)
            header(HttpHeaders.UserAgent, userAgent)
            requestHeaders.forEach { (name, value) -> header(name, value) }
            setBody(requestSerializer())
        }
        classifyResponse(response, parsePartialSuccess)
    }

    /**
     * Classifies a response according to the OTLP/HTTP spec.:
     *
     * See https://opentelemetry.io/docs/specs/otlp/
     */
    private suspend fun classifyResponse(
        response: HttpResponse,
        parsePartialSuccess: (body: ByteArray) -> OtlpPartialSuccess?,
    ): OtlpResponse {
        val code = response.status.value
        val body = response.boundedBodyBytes() ?: run {
            sdkErrorHandler.reportUserCodeError(
                IllegalStateException("OTLP response body exceeded $MAX_RESPONSE_BODY_BYTES bytes"),
                "OTLP response discarded (status=$code)",
            )
            return ResponseTooLarge(code)
        }
        return when (code) {
            in 200..299 -> when (val partialSuccess = parsePartialSuccess(body)) {
                null -> Success
                else -> PartialSuccess(partialSuccess.rejectedCount, partialSuccess.errorMessage)
            }

            429, 502, 503, 504 -> RetryableError(
                code,
                response.parseRetryAfterMs(),
                body.deserializeStatusMessage(),
            )

            in 400..499 -> ClientError(code, body.deserializeStatusMessage())
            in 500..599 -> ServerError(code, body.deserializeStatusMessage())
            else -> UnexpectedStatus(code)
        }
    }

    /**
     * Reads the response body, returning null if it exceeds [MAX_RESPONSE_BODY_BYTES].
     */
    private suspend fun HttpResponse.boundedBodyBytes(): ByteArray? =
        bodyAsChannel().readRemaining(MAX_RESPONSE_BODY_BYTES + 1).use { packet ->
            packet.readByteArray().takeIf { it.size <= MAX_RESPONSE_BODY_BYTES }
        }

    /**
     * Parses the Retry-After header (in seconds) as milliseconds.
     */
    private fun HttpResponse.parseRetryAfterMs(): Long? {
        val header = headers[HttpHeaders.RetryAfter] ?: return null
        return header.toLongOrNull()?.takeIf { it >= 0 }?.let { it * 1000L }
    }

    private companion object {
        const val MAX_RESPONSE_BODY_BYTES: Long = 4 * 1024 * 1024
    }
}
