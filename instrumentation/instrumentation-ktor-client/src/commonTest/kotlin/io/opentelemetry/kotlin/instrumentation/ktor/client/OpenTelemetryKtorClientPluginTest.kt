package io.opentelemetry.kotlin.instrumentation.ktor.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class OpenTelemetryKtorClientPluginTest {

    private val sdks = SdkTracker()

    private lateinit var processor: FakeSpanProcessor

    private val spans: List<SpanData>
        get() = processor.endCalls.map { it.toSpanData() }

    @BeforeTest
    fun setUp() {
        processor = FakeSpanProcessor()
    }

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun testRequestSuccess() = runTest {
        client(okEngine()).get("https://example.com/users")

        val span = spans.single()
        assertEquals("GET", span.name)
        assertEquals(SpanKind.CLIENT, span.spanKind)
    }

    @Test
    fun testRequestFail() = runTest {
        val client = client(MockEngine { throw BoomException() })

        assertFails { client.get("http://example.com/x") }

        val span = spans.single()
        assertTrue(span.isEnded)
    }

    @Test
    fun testOtlpExporterDoesNotTraceItsOwnRequests() = runTest {
        val openTelemetry = openTelemetry()
        val engine = MockEngine {
            respond("", HttpStatusCode.OK)
        }
        val httpClient = HttpClient(engine) {
            install(openTelemetryKtorClientPlugin(openTelemetry))
        }
        val exporter = fakeConfig().otlpHttpSpanExporter {
            endpoint = "http://localhost:4318"
            this.httpClient = httpClient
        }
        try {
            assertEquals(OperationResultCode.Success, exporter.export(listOf(FakeSpanData())))
            withContext(Dispatchers.Default) {
                assertEquals(OperationResultCode.Success, exporter.shutdown())
            }

            assertEquals(1, engine.requestHistory.size)
            assertTrue(spans.isEmpty(), "OTLP export HTTP must not create CLIENT spans: $spans")

            httpClient.get("https://example.com/users")
            val span = spans.single()
            assertEquals("GET", span.name)
            assertEquals(SpanKind.CLIENT, span.spanKind)
        } finally {
            withContext(NonCancellable) {
                httpClient.close()
                engine.close()
            }
        }
    }

    @Test
    fun testExportDoesNotSuppressConcurrentApplicationRequest() = runTest {
        val exportStarted = CompletableDeferred<Unit>()
        val releaseExport = CompletableDeferred<Unit>()
        val engine = MockEngine { request ->
            if (request.url.encodedPath.contains("v1/traces")) {
                exportStarted.complete(Unit)
                releaseExport.await()
            }
            respond("", HttpStatusCode.OK)
        }
        val openTelemetry = openTelemetry()
        val httpClient = HttpClient(engine) {
            install(openTelemetryKtorClientPlugin(openTelemetry))
        }
        val exporter = fakeConfig().otlpHttpSpanExporter {
            endpoint = "http://localhost:4318"
            this.httpClient = httpClient
        }
        try {
            assertEquals(OperationResultCode.Success, exporter.export(listOf(FakeSpanData())))
            withContext(Dispatchers.Default) {
                exportStarted.await()
            }

            httpClient.get("https://example.com/users")
            val applicationSpan = spans.single()
            assertEquals("GET", applicationSpan.name)
            assertEquals(SpanKind.CLIENT, applicationSpan.spanKind)

            releaseExport.complete(Unit)
            withContext(Dispatchers.Default) {
                assertEquals(OperationResultCode.Success, exporter.shutdown())
            }
            assertEquals(listOf("GET"), spans.map { it.name })
        } finally {
            if (!releaseExport.isCompleted) {
                releaseExport.complete(Unit)
            }
            withContext(NonCancellable) {
                httpClient.close()
                engine.close()
            }
        }
    }

    private fun openTelemetry(): OpenTelemetry =
        createOpenTelemetry {
            tracerProvider { export { processor } }
        }.also(sdks::track)

    private fun client(
        engine: MockEngine,
        openTelemetry: OpenTelemetry = openTelemetry(),
    ): HttpClient = HttpClient(engine) {
        install(openTelemetryKtorClientPlugin(openTelemetry))
    }

    private fun okEngine(): MockEngine = MockEngine { respond("ok", HttpStatusCode.OK) }

    private fun fakeConfig(): TraceExportConfigDsl = object : TraceExportConfigDsl {
        override val clock: Clock = FakeClock()
        override val sdkErrorHandler = NoopSdkErrorHandler
    }
}

private class BoomException : RuntimeException("boom")
