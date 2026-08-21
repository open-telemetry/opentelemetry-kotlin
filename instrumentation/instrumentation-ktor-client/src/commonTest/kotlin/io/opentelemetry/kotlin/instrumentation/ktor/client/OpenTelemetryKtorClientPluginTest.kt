package io.opentelemetry.kotlin.instrumentation.ktor.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class OpenTelemetryKtorClientPluginTest {

    private lateinit var processor: FakeSpanProcessor

    private val spans: List<SpanData>
        get() = processor.endCalls.map { it.toSpanData() }

    @BeforeTest
    fun setUp() {
        processor = FakeSpanProcessor()
    }

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
        assertTrue(span.hasEnded)
    }

    private fun openTelemetry(): OpenTelemetry =
        createOpenTelemetry {
            tracerProvider { export { processor } }
        }

    private fun client(
        engine: MockEngine,
        openTelemetry: OpenTelemetry = openTelemetry(),
    ): HttpClient = HttpClient(engine) {
        install(openTelemetryKtorClientPlugin(openTelemetry))
    }

    private fun okEngine(): MockEngine = MockEngine { respond("ok", HttpStatusCode.OK) }
}

private class BoomException : RuntimeException("boom")
