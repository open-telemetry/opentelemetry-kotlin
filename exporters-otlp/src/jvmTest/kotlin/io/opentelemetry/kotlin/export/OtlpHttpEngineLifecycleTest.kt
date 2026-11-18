package io.opentelemetry.kotlin.export

import com.sun.net.httpserver.HttpServer
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies SDK-created engines are closed on release. OkHttp's dispatcher threads are non-daemon
 * with a 60s keep-alive, so an unclosed engine delays JVM exit by up to a minute.
 */
internal class OtlpHttpEngineLifecycleTest {

    private val errorHandler = FakeSdkErrorHandler()

    @AfterTest
    fun tearDown() {
        HttpClientRegistry.clear()
        assertFalse(errorHandler.hasErrors(), "Unexpected SDK errors: ${errorHandler.errors}")
    }

    @Test
    fun testDefaultEngineThreadsStopAfterRelease() {
        HttpClientRegistry.clear()
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext("/v1/traces") { exchange ->
                exchange.requestBody.use { it.readBytes() }
                exchange.sendResponseHeaders(200, -1)
                exchange.close()
            }
            start()
        }
        try {
            val before = okHttpWorkerThreads()
            val client = createClient { endpoint = "http://localhost:${server.address.port}" }
            assertEquals(OtlpResponse.Success, runBlocking { client.exportTraces(listOf(FakeSpanData())) })
            val created = awaitValue { (okHttpWorkerThreads() - before).takeIf { it.isNotEmpty() } }

            client.release()

            awaitValue { created.none { it.isAlive }.takeIf { it } }
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun testSdkCreatedTlsEnginesAreClosedAndSuppliedOnesAreNot() {
        val suppliedEngine = OkHttp.create()
        val suppliedClient = HttpClient(OkHttp.create())
        try {
            val tls = mutableListOf<HttpClientEngine>()
            val client = createClient { installTls(tls) }
            assertTrue(tls.single().isActive)
            client.release()
            assertFalse(tls.single().isActive)

            tls.clear()
            createClient {
                installTls(tls)
                installTls(tls)
            }.release()
            assertTrue(tls.none { it.isActive })

            tls.clear()
            val withEngine = createClient {
                installTls(tls)
                httpClientEngine = suppliedEngine
            }
            val withClient = createClient {
                installTls(tls)
                httpClient = suppliedClient
            }
            assertTrue(tls.none { it.isActive })
            withEngine.release()
            withClient.release()
            assertTrue(suppliedEngine.isActive)
            assertTrue(suppliedClient.isActive)
        } finally {
            suppliedClient.close()
            suppliedEngine.close()
        }
    }

    private fun createClient(block: OtlpHttpExporterConfigDsl.() -> Unit): OtlpClient =
        createOtlpHttpClient(errorHandler, OtlpEndpoint.Traces, getEnvVar = { null }, block)

    private fun OtlpHttpExporterConfigDsl.installTls(into: MutableList<HttpClientEngine>) {
        configureTls(certificateFile)
        into += checkNotNull(httpClientEngine)
    }

    private fun okHttpWorkerThreads(): Set<Thread> =
        Thread.getAllStackTraces().keys.filterTo(mutableSetOf()) { it.name.startsWith("OkHttp") && !it.isDaemon }

    private fun <T : Any> awaitValue(timeoutMs: Long = 5_000, condition: () -> T?): T {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (true) {
            condition()?.let { return it }
            check(System.currentTimeMillis() < deadline) { "Condition not met within ${timeoutMs}ms" }
            Thread.sleep(20)
        }
    }

    private val certificateFile: String
        get() = checkNotNull(javaClass.getResource("/tls/cert.pem")).toURI().path
}
