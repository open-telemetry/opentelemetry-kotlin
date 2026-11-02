package io.opentelemetry.kotlin.instrumentation.ktor.server

import io.ktor.client.request.get
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OpenTelemetryKtorServerPluginTest {

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
    fun testRequestSuccess() = testApplication {
        server { get("/users") { call.respondText("ok") } }

        client.get("/users")

        val span = spans.single()
        assertEquals("GET", span.name)
        assertEquals(SpanKind.SERVER, span.spanKind)
    }

    @Test
    fun testRequestFail() = testApplication {
        server { get("/x") { throw BoomException() } }

        runCatching { client.get("/x") }

        // single() also asserts the span was not ended, and so recorded, more than once.
        val span = spans.single()
        assertTrue(span.hasEnded)
    }

    private fun openTelemetry(): OpenTelemetry =
        createOpenTelemetry {
            tracerProvider { export { processor } }
        }.also(sdks::track)

    private fun ApplicationTestBuilder.server(routes: Route.() -> Unit) {
        install(openTelemetryKtorServerPlugin(openTelemetry()))
        routing(routes)
    }
}

private class BoomException : RuntimeException("boom")
