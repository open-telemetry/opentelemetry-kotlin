package io.opentelemetry.kotlin.export

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.util.GZipEncoder
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.toByteArray
import io.opentelemetry.proto.collector.logs.v1.ExportLogsPartialSuccess
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * A fake OTLP/HTTP collector built on [MockEngine]. Each request consumes the next scripted
 * [Reply] and the final reply is repeated for any further requests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class FakeOtlpServer(
    private val scheduler: TestCoroutineScheduler,
    dispatcher: CoroutineDispatcher,
    vararg replies: Reply,
) {

    sealed interface Reply

    class Respond(
        val status: HttpStatusCode,
        val body: ByteArray = ByteArray(0),
        val headers: Headers = Headers.Empty,
        val gzip: Boolean = false,
    ) : Reply

    /** The connection drops without an HTTP response. */
    object Disconnect : Reply

    /** The server takes [delayMs] to respond, which can exceed the client's request timeout. */
    class Slow(val delayMs: Long, val reply: Respond) : Reply

    private val script = replies.toList().also { require(it.isNotEmpty()) }

    val requestTimes = mutableListOf<Long>()

    val requestUrls = mutableListOf<Url>()

    val requestCount: Int get() = requestTimes.size

    val engine = MockEngine(
        MockEngineConfig().apply {
            this.dispatcher = dispatcher
            addHandler { request ->
                requestTimes += scheduler.currentTime
                requestUrls += request.url
                var reply = script[minOf(requestTimes.size - 1, script.lastIndex)]
                if (reply is Slow) {
                    delay(reply.delayMs)
                    reply = reply.reply
                }
                when (reply) {
                    is Disconnect -> {
                        error("Connection reset by peer")
                    }

                    is Respond -> {
                        val body = if (reply.gzip) {
                            gzip(reply.body)
                        } else {
                            reply.body
                        }
                        val headers = if (reply.gzip) {
                            Headers.build {
                                appendAll(reply.headers)
                                append(HttpHeaders.ContentEncoding, "gzip")
                            }
                        } else {
                            reply.headers
                        }
                        respond(ByteReadChannel(body), reply.status, headers)
                    }

                    is Slow -> {
                        error("Nested Slow replies are not supported")
                    }
                }
            }
        }
    )

    fun intervals(): List<Long> = requestTimes.zipWithNext { a, b -> b - a }

    private suspend fun gzip(bytes: ByteArray): ByteArray =
        GZipEncoder.encode(ByteReadChannel(bytes), currentCoroutineContext()).toByteArray()

    companion object {
        private val protobufHeaders = headersOf(HttpHeaders.ContentType, "application/x-protobuf")

        fun traceSuccess(status: HttpStatusCode = HttpStatusCode.OK, gzip: Boolean = false) = Respond(
            status = status,
            body = ExportTraceServiceResponse.ADAPTER.encode(ExportTraceServiceResponse()),
            headers = protobufHeaders,
            gzip = gzip,
        )

        fun tracePartialSuccess(rejected: Long, message: String, gzip: Boolean = false) = Respond(
            status = HttpStatusCode.OK,
            body = ExportTraceServiceResponse.ADAPTER.encode(
                ExportTraceServiceResponse(partial_success = ExportTracePartialSuccess(rejected, message))
            ),
            headers = protobufHeaders,
            gzip = gzip,
        )

        fun logPartialSuccess(rejected: Long, message: String) = Respond(
            status = HttpStatusCode.OK,
            body = ExportLogsServiceResponse.ADAPTER.encode(
                ExportLogsServiceResponse(partial_success = ExportLogsPartialSuccess(rejected, message))
            ),
            headers = protobufHeaders,
        )

        fun failure(
            status: HttpStatusCode,
            message: String = status.description,
            grpcCode: Int = 0,
            retryAfter: String? = null,
            withBadRequestDetails: Boolean = false,
        ) = Respond(
            status = status,
            body = encodeStatus(grpcCode, message, withBadRequestDetails),
            headers = Headers.build {
                appendAll(protobufHeaders)
                retryAfter?.let { append(HttpHeaders.RetryAfter, it) }
            },
        )

        fun proxyErrorPage(status: HttpStatusCode, retryAfter: String? = null) = Respond(
            status = status,
            body = "<html><body><h1>${status.value} ${status.description}</h1></body></html>".encodeToByteArray(),
            headers = Headers.build {
                append(HttpHeaders.ContentType, "text/html")
                retryAfter?.let { append(HttpHeaders.RetryAfter, it) }
            },
        )

        fun redirect(status: HttpStatusCode) = Respond(
            status = status,
            headers = headersOf(HttpHeaders.Location, "http://localhost:4318/moved"),
        )

        /**
         * Encodes a `google.rpc.Status` (code = 1, message = 2, details = 3).
         */
        private fun encodeStatus(code: Int, message: String, withBadRequestDetails: Boolean): ByteArray {
            var bytes = varintField(1, code.toLong()) + bytesField(2, message.encodeToByteArray())
            if (withBadRequestDetails) {
                val fieldViolation = bytesField(1, "resource_spans[0].scope_spans[0].spans[0].trace_id".encodeToByteArray()) +
                    bytesField(2, "trace_id must be 16 bytes".encodeToByteArray())
                val badRequest = bytesField(1, fieldViolation)
                val any = bytesField(1, "type.googleapis.com/google.rpc.BadRequest".encodeToByteArray()) +
                    bytesField(2, badRequest)
                bytes += bytesField(3, any)
            }
            return bytes
        }

        private fun varintField(fieldNumber: Int, value: Long): ByteArray =
            if (value == 0L) {
                ByteArray(0)
            } else {
                varint((fieldNumber shl 3).toLong()) + varint(value)
            }

        private fun bytesField(fieldNumber: Int, value: ByteArray): ByteArray =
            varint(((fieldNumber shl 3) or 2).toLong()) + varint(value.size.toLong()) + value

        private fun varint(value: Long): ByteArray {
            val out = mutableListOf<Byte>()
            var remaining = value
            while (remaining and 0x7FL.inv() != 0L) {
                out += ((remaining and 0x7F) or 0x80).toByte()
                remaining = remaining ushr 7
            }
            out += remaining.toByte()
            return out.toByteArray()
        }
    }
}
