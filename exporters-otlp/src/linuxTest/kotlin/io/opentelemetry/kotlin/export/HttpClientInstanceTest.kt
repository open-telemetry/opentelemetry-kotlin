package io.opentelemetry.kotlin.export

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class HttpClientInstanceTest {
    @Test
    fun testDefaultEngineCanReachLocalServer() = runBlocking {
        withTimeout(10.seconds) {
            val selector = SelectorManager(Dispatchers.IO)
            val server = aSocket(selector).tcp().bind("127.0.0.1", 0)
            val client = createDefaultHttpClient(requestTimeoutMs = 5_000)
            try {
                val response = launch {
                    val socket = server.accept()
                    try {
                        val input = socket.openReadChannel()
                        assertEquals("GET /health HTTP/1.1", input.readUTF8Line())
                        while (!input.readUTF8Line().isNullOrEmpty()) {
                            // Read the remaining HTTP request headers.
                        }
                        socket.openWriteChannel(autoFlush = true).writeStringUtf8(
                            "HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: close\r\n\r\nok"
                        )
                    } finally {
                        socket.close()
                    }
                }
                val port = (server.localAddress as InetSocketAddress).port
                assertEquals("ok", client.get("http://127.0.0.1:$port/health").bodyAsText())
                response.join()
            } finally {
                client.close()
                server.close()
                selector.close()
            }
        }
    }
}
