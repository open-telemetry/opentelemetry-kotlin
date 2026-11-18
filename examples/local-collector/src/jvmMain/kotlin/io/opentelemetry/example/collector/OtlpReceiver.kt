package io.opentelemetry.example.collector

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.opentelemetry.kotlin.proto.collector.logs.v1.ExportLogsServiceRequest
import io.opentelemetry.kotlin.proto.collector.logs.v1.ExportLogsServiceResponse
import io.opentelemetry.kotlin.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.kotlin.proto.collector.trace.v1.ExportTraceServiceResponse
import java.util.zip.GZIPInputStream

private val protobuf = ContentType("application", "x-protobuf")

/**
 * A minimal OTLP/HTTP receiver that accepts protobuf-encoded traces and logs on
 * `/v1/traces` and `/v1/logs`. JSON encoding and metrics are not supported.
 */
fun startOtlpReceiver(
    port: Int,
    onSpans: (List<ReceivedSpan>) -> Unit,
    onLogs: (List<ReceivedLog>) -> Unit,
): EmbeddedServer<*, *> = embeddedServer(CIO, port = port, host = "0.0.0.0") {
    routing {
        post("/v1/traces") {
            call.handle {
                onSpans(ExportTraceServiceRequest.ADAPTER.decode(it).toReceivedSpans())
                ExportTraceServiceResponse().encode()
            }
        }
        post("/v1/logs") {
            call.handle {
                onLogs(ExportLogsServiceRequest.ADAPTER.decode(it).toReceivedLogs())
                ExportLogsServiceResponse().encode()
            }
        }
    }
}.start(wait = false)

private suspend fun ApplicationCall.handle(decode: (ByteArray) -> ByteArray) {
    val contentType = request.header(HttpHeaders.ContentType)
    if (contentType != null && !ContentType.parse(contentType).match(protobuf)) {
        respondText("Only application/x-protobuf is supported", status = HttpStatusCode.UnsupportedMediaType)
        return
    }
    val response = runCatching {
        var body = receive<ByteArray>()
        if (request.header(HttpHeaders.ContentEncoding).equals("gzip", ignoreCase = true)) {
            body = GZIPInputStream(body.inputStream()).use { it.readBytes() }
        }
        decode(body)
    }
    response.fold(
        onSuccess = { respondBytes(it, protobuf) },
        onFailure = { respondText("Malformed request: ${it.message}", status = HttpStatusCode.BadRequest) },
    )
}
