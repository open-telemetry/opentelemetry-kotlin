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
import io.opentelemetry.kotlin.export.OtlpResponse.ServerError
import io.opentelemetry.kotlin.export.OtlpResponse.Success
import io.opentelemetry.kotlin.export.OtlpResponse.Unknown
import io.opentelemetry.kotlin.logging.export.deserializeLogRecordErrorMessage
import io.opentelemetry.kotlin.logging.export.toProtobufByteArray
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.deserializeTraceRecordErrorMessage
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
        ByteArray::deserializeLogRecordErrorMessage
    )

    suspend fun exportTraces(telemetry: List<SpanData>): OtlpResponse = exportTelemetry(
        OtlpEndpoint.Traces,
        telemetry::toProtobufByteArray,
        ByteArray::deserializeTraceRecordErrorMessage
    )

    private suspend fun exportTelemetry(
        endpoint: OtlpEndpoint,
        requestSerializer: () -> ByteArray,
        onError: (body: ByteArray) -> String?,
    ): OtlpResponse {
        return try {
            val url = "$baseUrl/${endpoint.path}"
            val response = httpClient.post(url) {
                compress("gzip")
                contentType(contentType)
                header(HttpHeaders.UserAgent, userAgent)
                setBody(requestSerializer())
            }
            when (val code = response.status.value) {
                200 -> Success
                in 400..499 -> ClientError(code, onError(response.boundedBodyBytes()))
                in 500..599 -> ServerError(code, onError(response.boundedBodyBytes()))
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

    private companion object {
        const val MAX_ERROR_BODY_BYTES: Long = 4 * 1024 * 1024
    }
}
