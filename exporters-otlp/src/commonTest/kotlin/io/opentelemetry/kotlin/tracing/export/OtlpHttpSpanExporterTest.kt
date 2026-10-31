package io.opentelemetry.kotlin.tracing.export

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.HttpTimeoutConfig.Companion.INFINITE_TIMEOUT_MS
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
import io.opentelemetry.kotlin.export.createHttpEngine
import io.opentelemetry.kotlin.export.createTestHttpClient
import io.opentelemetry.kotlin.export.decodeOtlpRequestBody
import io.opentelemetry.kotlin.export.testMockEngine
import io.opentelemetry.kotlin.export.withShutdown
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import io.opentelemetry.kotlin.ioDispatcher
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OtlpHttpSpanExporterTest {

    private val spans = listOf(FakeSpanData())
    private val baseUrl = "http://localhost:1234"

    private lateinit var client: OtlpClient
    private lateinit var server: MockEngine
    private lateinit var mockResponseStatus: HttpStatusCode
    private var serverDelayMs: Long = 0
    private val requestBody = CompletableDeferred<ByteArray>()

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        server = testMockEngine(dispatcher) {
            delay(serverDelayMs)
            val response = respond(
                content = ByteReadChannel(""),
                status = mockResponseStatus
            )
            // Body transformations are tied to the request context, so capture the body here;
            // reading it later fails after the request completes.
            requestBody.complete(it.body.toByteArray())
            response
        }
        val httpClient = createTestHttpClient(INFINITE_TIMEOUT_MS, server, dispatcher)
        client = OtlpClient(baseUrl, httpClient = httpClient, sdkErrorHandler = NoopSdkErrorHandler)
    }

    private fun exporter() = OtlpHttpSpanExporter(
        client,
        initialDelayMs = 3,
        maxAttemptIntervalMs = 5,
        maxAttempts = 3,
        sdkErrorHandler = NoopSdkErrorHandler,
        coroutineContext = dispatcher,
        timeSource = dispatcher.scheduler.timeSource
    )

    @Test
    fun testExportInitialSuccess() = runTest(dispatcher) {
        exporter().withShutdown { exporter ->
            mockResponseStatus = HttpStatusCode.OK
            val code = exporter.export(spans)
            assertEquals(OperationResultCode.Success, code)
            waitAndAssertExportedTelemetry(spans)
        }
    }

    @Test
    fun testExportForceFlush() = runTest {
        exporter().withShutdown { exporter ->
            val code = exporter.forceFlush()
            assertEquals(OperationResultCode.Success, code)
        }
    }

    @Test
    fun testShutdownDrainsPendingTelemetry() = runTest(dispatcher) {
        exporter().withShutdown { exporter ->
            mockResponseStatus = HttpStatusCode.OK
            serverDelayMs = 1000
            val code = exporter.export(spans)
            assertEquals(OperationResultCode.Success, code)
            val shutdownCode = exporter.shutdown()
            assertEquals(OperationResultCode.Success, shutdownCode)
            requestBody.await()
            assertEquals(1, server.requestHistory.size)
        }
    }

    @Test
    fun testExportRetryAttempts() = runTest(dispatcher) {
        exporter().withShutdown { exporter ->
            mockResponseStatus = HttpStatusCode.OK
            serverDelayMs = 2
            val code = exporter.export(spans)
            assertEquals(OperationResultCode.Success, code)
            waitAndAssertExportedTelemetry(spans)
        }
    }

    @Test
    fun testExportNoOpOnEmpty() = runTest {
        exporter().withShutdown { exporter ->
            val code = exporter.export(emptyList())
            assertEquals(OperationResultCode.Success, code)
            assertTrue(server.requestHistory.isEmpty())
        }
    }

    @Test
    fun testCustomHttpClientIsUsed() = runTest {
        val request = CompletableDeferred<HttpRequestData>()
        val customServer = MockEngine {
            val response = respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
            request.complete(it)
            response
        }
        val customClient = HttpClient(customServer) {
            defaultRequest { header("Authorization", "Bearer test-token") }
        }
        val customExporter = fakeConfig().otlpHttpSpanExporter {
            endpoint = baseUrl
            httpClient = customClient
        }
        try {
            customExporter.export(spans)

            val headers = request.await().headers.toMap().mapValues { it.value.joinToString() }
            assertEquals("Bearer test-token", headers["Authorization"])
        } finally {
            withContext(NonCancellable) {
                customExporter.shutdown()
                customClient.close()
                customServer.close()
            }
        }
    }

    @Test
    fun testDefaultFactoryUsesSharedRegistryClient() = runTest {
        HttpClientRegistry.clear()
        val config = fakeConfig()
        try {
            // First exporter creation populates the registry with the default engine/client.
            val exporter1 = config.otlpHttpSpanExporter { endpoint = baseUrl }
            exporter1.withShutdown {
                val client1 = HttpClientRegistry.getOrCreate(requestTimeoutMs = EXPORT_REQUEST_TIMEOUT_MS)
                // Second exporter creation should reuse the cached client.
                val exporter2 = config.otlpHttpSpanExporter { endpoint = baseUrl }
                exporter2.withShutdown {
                    val client2 = HttpClientRegistry.getOrCreate(requestTimeoutMs = EXPORT_REQUEST_TIMEOUT_MS)
                    assertSame(client1, client2)
                }
            }
        } finally {
            HttpClientRegistry.clear()
        }
    }

    @Test
    fun testFactoryWithCustomEngineExports() = runTest {
        val mockServer = MockEngine {
            respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
        }
        val factoryExporter = fakeConfig().otlpHttpSpanExporter {
            endpoint = baseUrl
            httpClientEngine = mockServer
        }
        try {
            val code = factoryExporter.export(spans)
            assertEquals(OperationResultCode.Success, code)
            // export() only enqueues; shutdown() waits for the background export to finish. The DSL exporter
            // does real IO, so shutdown must run on real time, not the test's virtual time.
            val shutdownCode = withContext(Dispatchers.Default) { factoryExporter.shutdown() }
            assertEquals(OperationResultCode.Success, shutdownCode)
            assertEquals(1, mockServer.requestHistory.size)
        } finally {
            withContext(NonCancellable) {
                factoryExporter.shutdown()
                HttpClientRegistry.clear()
                mockServer.close()
            }
        }
    }

    @Test
    fun testHttpClientRegistryIsReused() {
        val engine = createHttpEngine()
        val client1 = HttpClientRegistry.getOrCreate(
            engine = engine,
            requestTimeoutMs = 30_000,
        )

        val client2 = HttpClientRegistry.getOrCreate(
            engine = engine,
            requestTimeoutMs = 30_000,
        )

        assertSame(client1, client2)
        HttpClientRegistry.clear()
    }

    @Test
    fun testHttpClientRegistryConcurrency() = runTest {
        val engine = createHttpEngine()

        val clients = coroutineScope {
            (1..50).map {
                async(ioDispatcher) {
                    HttpClientRegistry.getOrCreate(
                        engine = engine,
                        requestTimeoutMs = 30_000
                    )
                }
            }.awaitAll()
        }

        assertTrue(clients.all { it === clients.first() })
        HttpClientRegistry.clear()
    }

    private suspend fun waitAndAssertExportedTelemetry(telemetry: List<SpanData>) {
        val body = requestBody.await()
        val requests = server.requestHistory
        check(server.requestHistory.size == 1) {
            "Expected 1 request, got ${requests.size}"
        }
        val bytes = decodeOtlpRequestBody(requests.single(), body)
        assertContentEquals(telemetry.toProtobufByteArray(), bytes)
    }

    private fun fakeConfig(): TraceExportConfigDsl = object : TraceExportConfigDsl {
        override val clock: Clock = FakeClock()
        override val sdkErrorHandler = NoopSdkErrorHandler
    }
}
