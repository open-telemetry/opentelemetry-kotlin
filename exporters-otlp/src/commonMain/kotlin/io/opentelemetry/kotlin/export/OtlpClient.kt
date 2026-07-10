package io.opentelemetry.kotlin.export

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
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
import io.opentelemetry.kotlin.export.OtlpClient.Companion.MAX_ERROR_BODY_BYTES
import io.opentelemetry.kotlin.export.OtlpResponse.ClientError
import io.opentelemetry.kotlin.export.OtlpResponse.PartialSuccess
import io.opentelemetry.kotlin.export.OtlpResponse.RetryableError
import io.opentelemetry.kotlin.export.OtlpResponse.ServerError
import io.opentelemetry.kotlin.export.OtlpResponse.Success
import io.opentelemetry.kotlin.export.OtlpResponse.Unknown
import io.opentelemetry.kotlin.logging.export.deserializeLogRecordPartialSuccess
import io.opentelemetry.kotlin.logging.export.toProtobufByteArray
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.deserializeTraceRecordPartialSuccess
import io.opentelemetry.kotlin.tracing.export.toProtobufByteArray
import kotlinx.io.readByteArray

internal class OtlpClient(
    private val baseUrl: String,
    private val httpClient: HttpClient = defaultHttpClient
) {

    private val contentType = ContentType.parse("application/x-protobuf")
    private val userAgent = "OTel-OTLP-Exporter-Kotlin/${BuildKonfig.VERSION}"

    suspend fun exportLogs(telemetry: List<ReadableLogRecord>): OtlpResponse = exportTelemetry(
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
    ): OtlpResponse {
        return try {
            val url = "$baseUrl/${endpoint.path}"
            val response = httpClient.post(url) {
                compress("gzip")
                contentType(contentType)
                header(HttpHeaders.UserAgent, userAgent)
                setBody(requestSerializer())
            }
            // A 200 can still be a partial success and error responses can carry an error message,
            // so the body is always parsed rather than relying on the status code alone (see #558).
            val body = parsePartialSuccess(response.boundedBodyBytes())
            when (val code = response.status.value) {
                200 -> when (body) {
                    null -> Success
                    else -> PartialSuccess(body.rejectedCount, body.errorMessage)
                }
                429, 502, 503, 504 -> RetryableError(
                    code,
                    response.parseRetryAfterMs(),
                    body?.errorMessage,
                )
                in 400..499 -> ClientError(code, body?.errorMessage)
                in 500..599 -> ServerError(code, body?.errorMessage)
                else -> Unknown
            }
        } catch (ignored: HttpRequestTimeoutException) {
            Unknown
        }
    }

    /**
     * Reads at most [MAX_ERROR_BODY_BYTES] from the response body.
     */
    private suspend fun HttpResponse.boundedBodyBytes(): ByteArray =
        bodyAsChannel().readRemaining(MAX_ERROR_BODY_BYTES).readByteArray()

    /**
     * Parses the Retry-After header (in seconds) as milliseconds.
     */
    private fun HttpResponse.parseRetryAfterMs(): Long? {
        val header = headers[HttpHeaders.RetryAfter] ?: return null
        return header.toLongOrNull()?.takeIf { it >= 0 }?.let { it * 1000L }
    }

    private companion object {
        const val MAX_ERROR_BODY_BYTES: Long = 4 * 1024 * 1024
    }
}
