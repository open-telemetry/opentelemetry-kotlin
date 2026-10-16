package io.opentelemetry.kotlin.export

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readFully
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.logging.export.toProtobufByteArray
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.export.toProtobufByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

internal class HttpClientInstanceTest {
    @Test
    fun testDefaultEnginePostsUncompressedTracesAndLogs() = runBlocking {
        withTimeout(10.seconds) {
            val spans = listOf(FakeSpanData())
            val logs = listOf(FakeLogRecordData())
            val requests = listOf(
                OtlpEndpoint.Traces to spans.toProtobufByteArray(),
                OtlpEndpoint.Logs to logs.toProtobufByteArray(),
            )
            val selector = SelectorManager(Dispatchers.IO)
            val server = aSocket(selector).tcp().bind("127.0.0.1", 0)
            val httpClient = createDefaultHttpClient(requestTimeoutMs = 5_000)
            try {
                val receiver = launch {
                    for ((endpoint, body) in requests) {
                        val socket = server.accept()
                        try {
                            assertRequest(socket.openReadChannel(), endpoint, body)
                            socket.openWriteChannel(autoFlush = true).writeStringUtf8(
                                "HTTP/1.1 200 OK\r\nContent-Type: application/x-protobuf\r\n" +
                                        "Content-Length: 0\r\nConnection: close\r\n\r\n"
                            )
                        } finally {
                            socket.close()
                        }
                    }
                }
                val port = (server.localAddress as InetSocketAddress).port
                val client = OtlpClient("http://127.0.0.1:$port", httpClient, NoopSdkErrorHandler)
                assertIs<OtlpResponse.Success>(client.exportTraces(spans))
                assertIs<OtlpResponse.Success>(client.exportLogs(logs))
                receiver.join()
            } finally {
                httpClient.close()
                server.close()
                selector.close()
            }
        }
    }

    private suspend fun assertRequest(input: ByteReadChannel, endpoint: OtlpEndpoint, expectedBody: ByteArray) {
        assertEquals("POST /${endpoint.path} HTTP/1.1", input.readUTF8Line())
        val headers = buildMap {
            var line = input.readUTF8Line()
            while (!line.isNullOrEmpty()) {
                val (name, value) = line.split(':', limit = 2)
                put(name.lowercase(), value.trim())
                line = input.readUTF8Line()
            }
        }
        assertEquals("application/x-protobuf", headers["content-type"])
        assertEquals("identity", headers["accept-encoding"])
        assertNull(headers["content-encoding"])
        val body = ByteArray(checkNotNull(headers["content-length"]).toInt())
        input.readFully(body)
        assertContentEquals(expectedBody, body)
    }
}
