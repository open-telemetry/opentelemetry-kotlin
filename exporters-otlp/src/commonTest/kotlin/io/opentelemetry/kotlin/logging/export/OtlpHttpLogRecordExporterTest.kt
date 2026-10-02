package io.opentelemetry.kotlin.logging.export

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.util.GZipEncoder
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.toByteArray
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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
    private var serverDelayMs: Long = 0
    private var compressedRequestBody: ByteArray = ByteArray(0)

    @BeforeTest
    fun setUp() {
        server = MockEngine {
            // gzip compression is lazy and tied to the request context, so capture the body here;
            // reading it later fails after the request completes.
            compressedRequestBody = it.body.toByteArray()
            delay(serverDelayMs)
            respond(
                content = ByteReadChannel(""),
                status = mockResponseStatus
            )
        }
        val httpClient = createDefaultHttpClient(engine = server, requestTimeoutMs = 10_000)
        client = OtlpClient(baseUrl, httpClient = httpClient, sdkErrorHandler = NoopSdkErrorHandler)
    }

    private fun exporter() = OtlpHttpLogRecordExporter(
        client,
        initialDelayMs = 3,
        maxAttemptIntervalMs = 5,
        maxAttempts = 3,
        sdkErrorHandler = NoopSdkErrorHandler,
    )

    @Test
    fun testExportInitialSuccess() = runTest {
        withContext(Dispatchers.Default) {
            val exporter = exporter()
            mockResponseStatus = HttpStatusCode.OK
            val code = exporter.export(logRecords)
            assertEquals(OperationResultCode.Success, code)
            waitAndAssertExportedTelemetry(logRecords)
            exporter.shutdown()
        }
    }

    @Test
    fun testExportForceFlush() = runTest {
        withContext(Dispatchers.Default) {
            val exporter = exporter()
            val code = exporter.forceFlush()
            assertEquals(OperationResultCode.Success, code)
            exporter.shutdown()
        }
    }

    @Test
    fun testShutdownDrainsPendingTelemetry() = runTest {
        withContext(Dispatchers.Default) {
            val exporter = exporter()
            mockResponseStatus = HttpStatusCode.OK
            serverDelayMs = 1000
            val code = exporter.export(logRecords)
            assertEquals(OperationResultCode.Success, code)

            val shutdownCode = exporter.shutdown()
            assertEquals(OperationResultCode.Success, shutdownCode)

            withTimeout(10) {
                assertEquals(server.requestHistory.size, 1)
            }
            exporter.shutdown()
        }
    }

    @Test
    fun testExportRetryAttempts() = runTest {
        withContext(Dispatchers.Default) {
            val exporter = exporter()
            mockResponseStatus = HttpStatusCode.OK
            serverDelayMs = 2
            val code = exporter.export(logRecords)
            assertEquals(OperationResultCode.Success, code)
            waitAndAssertExportedTelemetry(logRecords)
            exporter.shutdown()
        }
    }

    @Test
    fun testExportNoOpOnEmpty() = runTest {
        withContext(Dispatchers.Default) {
            val exporter = exporter()
            val code = exporter.export(emptyList())
            assertEquals(OperationResultCode.Success, code)
            assertTrue(server.requestHistory.isEmpty())
            exporter.shutdown()
        }
    }

    @Test
    fun testCustomHttpClientIsUsed() = runTest {
        withContext(Dispatchers.Default) {
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
            customExporter.shutdown()
        }
    }

    @Test
    fun testDefaultFactoryUsesSharedRegistryClient() = runTest {
        HttpClientRegistry.clear()
        val config = fakeConfig()

        // 1. First exporter creation populates the registry with the default engine/client
        val exporter1 = config.otlpHttpLogRecordExporter { endpoint = baseUrl }
        val client1 = HttpClientRegistry.getOrCreate(requestTimeoutMs = EXPORT_REQUEST_TIMEOUT_MS)

        // 2. Second exporter creation should hit the existing cache without replacing it
        val exporter2 = config.otlpHttpLogRecordExporter { endpoint = baseUrl }
        val client2 = HttpClientRegistry.getOrCreate(requestTimeoutMs = EXPORT_REQUEST_TIMEOUT_MS)

        assertSame(client1, client2)
        HttpClientRegistry.clear()
        exporter1.shutdown()
        exporter2.shutdown()
    }

    @Test
    fun testFactoryWithCustomEngineExports() = runTest {
        withContext(Dispatchers.Default) {
            val mockServer = MockEngine {
                respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
            }
            val factoryExporter = fakeConfig().otlpHttpLogRecordExporter {
                endpoint = baseUrl
                httpClientEngine = mockServer
            }
            val code = factoryExporter.export(logRecords)
            assertEquals(OperationResultCode.Success, code)

            withTimeout(5000) {
                while (mockServer.requestHistory.isEmpty()) {
                    delay(1L)
                }
            }
            assertEquals(1, mockServer.requestHistory.size)
            HttpClientRegistry.clear()
            factoryExporter.shutdown()
        }
    }

    private suspend fun waitAndAssertExportedTelemetry(
        telemetry: List<LogRecordData>,
        timeoutMs: Long = 1000
    ) {
        // use real time because the exporter runs on Dispatchers.Default.
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(timeoutMs) {
                while (server.requestHistory.isEmpty()) {
                    delay(1L)
                }
            }
        }
        val requests = server.requestHistory
        check(server.requestHistory.size == 1) {
            "Expected 1 request, got ${requests.size}"
        }
        val bytes = GZipEncoder.decode(ByteReadChannel(compressedRequestBody))
            .toByteArray()
        assertContentEquals(telemetry.toProtobufByteArray(), bytes)
    }

    private fun fakeConfig(): LogExportConfigDsl = object : LogExportConfigDsl {
        override val clock: Clock = FakeClock()
        override val sdkErrorHandler = NoopSdkErrorHandler
    }
}
