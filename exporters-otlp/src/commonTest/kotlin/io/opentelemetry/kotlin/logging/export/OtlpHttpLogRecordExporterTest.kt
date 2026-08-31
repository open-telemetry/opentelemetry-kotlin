package io.opentelemetry.kotlin.logging.export

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.EXPORT_REQUEST_TIMEOUT_MS
import io.opentelemetry.kotlin.export.HttpClientRegistry
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OtlpClient
import io.opentelemetry.kotlin.export.createDefaultHttpClient
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.logging.data.LogRecordData
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OtlpHttpLogRecordExporterTest {

    private val logRecords = listOf(FakeLogRecordData())
    private val baseUrl = "http://localhost:1234"

    private lateinit var client: OtlpClient
    private lateinit var server: MockEngine
    private lateinit var mockResponseStatus: HttpStatusCode
    private lateinit var exporter: OtlpHttpLogRecordExporter
    private var serverDelayMs: Long = 0

    @BeforeTest
    fun setUp() {
        server = MockEngine {
            delay(serverDelayMs)
            respond(
                content = ByteReadChannel(""),
                status = mockResponseStatus
            )
        }
        val httpClient = createDefaultHttpClient(engine = server, requestTimeoutMs = 10_000)
        client = OtlpClient(baseUrl, httpClient = httpClient, sdkErrorHandler = NoopSdkErrorHandler)
        exporter = OtlpHttpLogRecordExporter(
            client,
            initialDelayMs = 3,
            maxAttemptIntervalMs = 5,
            maxAttempts = 3,
            sdkErrorHandler = NoopSdkErrorHandler,
        )
    }

    @Test
    fun testExportInitialSuccess() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        val code = exporter.export(logRecords)
        assertEquals(OperationResultCode.Success, code)
        assertTelemetryExported(logRecords)
    }

    @Test
    fun testExportForceFlush() = runTest {
        val code = exporter.forceFlush()
        assertEquals(OperationResultCode.Success, code)
    }

    @Test
    fun testExportShutdown() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        serverDelayMs = 1000
        val code = exporter.export(logRecords)
        assertEquals(OperationResultCode.Success, code)

        val shutdownCode = exporter.shutdown()
        assertEquals(OperationResultCode.Success, shutdownCode)

        withTimeout(10) {
            assertTrue(server.requestHistory.isEmpty())
        }
    }

    @Test
    fun testExportRetryAttempts() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        serverDelayMs = 2
        val code = exporter.export(logRecords)
        assertEquals(OperationResultCode.Success, code)
        assertTelemetryExported(logRecords)
    }

    @Test
    fun testExportNoOpOnEmpty() = runTest {
        val code = exporter.export(emptyList())
        assertEquals(OperationResultCode.Success, code)
        assertTrue(server.requestHistory.isEmpty())
    }

    @Test
    fun testCustomHttpClientIsUsed() = runTest {
        val customServer = MockEngine {
            respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
        }
        val customClient = HttpClient(customServer) {
            defaultRequest { header("Authorization", "Bearer test-token") }
        }
        val customExporter = fakeConfig().otlpHttpLogRecordExporter {
            endpoint = baseUrl
            httpClient = customClient
        }
        customExporter.export(logRecords)

        withTimeout(1000) {
            while (customServer.requestHistory.isEmpty()) {
                delay(1L)
            }
        }
        val headers = customServer.requestHistory.single().headers.toMap().mapValues { it.value.joinToString() }
        assertEquals("Bearer test-token", headers["Authorization"])
    }

    @Test
    fun testDefaultFactoryUsesSharedRegistryClient() {
        HttpClientRegistry.clear()
        val config = fakeConfig()

        // 1. First exporter creation populates the registry with the default engine/client
        config.otlpHttpLogRecordExporter { endpoint = baseUrl }
        val client1 = HttpClientRegistry.getOrCreate(requestTimeoutMs = EXPORT_REQUEST_TIMEOUT_MS)

        // 2. Second exporter creation should hit the existing cache without replacing it
        config.otlpHttpLogRecordExporter { endpoint = baseUrl }
        val client2 = HttpClientRegistry.getOrCreate(requestTimeoutMs = EXPORT_REQUEST_TIMEOUT_MS)

        assertSame(client1, client2)
        HttpClientRegistry.clear()
    }

    @Test
    fun testFactoryWithCustomEngineExports() = runTest {
        val mockServer = MockEngine {
            respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
        }
        val factoryExporter = fakeConfig().otlpHttpLogRecordExporter {
            endpoint = baseUrl
            httpClientEngine = mockServer
        }
        val code = factoryExporter.export(logRecords)
        assertEquals(OperationResultCode.Success, code)

        withTimeout(1000) {
            while (mockServer.requestHistory.isEmpty()) {
                delay(1L)
            }
        }
        assertEquals(1, mockServer.requestHistory.size)
        HttpClientRegistry.clear()
    }

    private suspend fun waitForExportedTelemetry(
        telemetrySize: Int = 1,
        timeoutMs: Long = 1000,
    ): List<HttpRequestData> {
        withTimeout(timeoutMs) {
            while (server.requestHistory.size < telemetrySize) {
                delay(1L)
            }
        }
        val requests = server.requestHistory
        check(requests.size == telemetrySize) {
            "Expected 1 request, got ${requests.size}"
        }
        return requests
    }

    private suspend fun assertTelemetryExported(telemetry: List<LogRecordData>) {
        val requests = waitForExportedTelemetry()
        val request = requests.single()
        val bytes = request.body.toByteArray()
        assertContentEquals(telemetry.toProtobufByteArray(), bytes)
    }

    private fun fakeConfig(): LogExportConfigDsl = object : LogExportConfigDsl {
        override val clock: Clock = FakeClock()
        override val sdkErrorHandler = NoopSdkErrorHandler
    }
}
