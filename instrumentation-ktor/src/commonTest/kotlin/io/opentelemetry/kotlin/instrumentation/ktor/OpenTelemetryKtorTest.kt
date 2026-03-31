package io.opentelemetry.kotlin.instrumentation.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.semconv.HttpAttributes
import io.opentelemetry.kotlin.semconv.ServerAttributes
import io.opentelemetry.kotlin.semconv.UrlAttributes
import io.opentelemetry.kotlin.tracing.StatusCode
import io.opentelemetry.kotlin.tracing.export.InMemorySpanExporter
import io.opentelemetry.kotlin.tracing.export.inMemorySpanExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
class OpenTelemetryKtorTest {

    @Test
    fun testInstrumentation() = runTest {
        var inMemoryExporter: InMemorySpanExporter? = null
        val otel = createOpenTelemetry {
            tracerProvider {
                export {
                    val exporter = inMemorySpanExporter()
                    inMemoryExporter = exporter
                    simpleSpanProcessor(exporter)
                }
            }
        }

        val client = HttpClient(MockEngine) {
            install(OpenTelemetryKtor) {
                openTelemetry = otel
            }
            engine {
                addHandler { _ ->
                    respond("OK", HttpStatusCode.OK)
                }
            }
        }

        client.get("https://example.com/test")

        val spans = inMemoryExporter?.exportedSpans ?: emptyList()
        assertEquals(1, spans.size)
        val span = spans[0]

        assertEquals("GET", span.name)
        assertEquals("GET", span.attributes[HttpAttributes.HTTP_REQUEST_METHOD])
        assertEquals("https://example.com/test", span.attributes[UrlAttributes.URL_FULL])
        assertEquals("example.com", span.attributes[ServerAttributes.SERVER_ADDRESS])
        assertEquals(200L, span.attributes[HttpAttributes.HTTP_RESPONSE_STATUS_CODE])
        assertNotNull(span.endTimestamp)
        assertEquals(StatusCode.OK, span.status.statusCode)
    }

    @Test
    fun testPostWithBody() = runTest {
        var inMemoryExporter: InMemorySpanExporter? = null
        val otel = createOpenTelemetry {
            tracerProvider {
                export {
                    val exporter = inMemorySpanExporter()
                    inMemoryExporter = exporter
                    simpleSpanProcessor(exporter)
                }
            }
        }

        val client = HttpClient(MockEngine) {
            install(OpenTelemetryKtor) {
                openTelemetry = otel
            }
            engine {
                addHandler { _ ->
                    respond("Created", HttpStatusCode.Created)
                }
            }
        }

        client.get("https://example.com/post") {
            setBody("Hello World")
        }

        val spans = inMemoryExporter?.exportedSpans ?: emptyList()
        assertEquals(1, spans.size)
        val span = spans[0]

        assertEquals("GET", span.name) // Ktor get() is used here, but with body
        assertEquals(201L, span.attributes[HttpAttributes.HTTP_RESPONSE_STATUS_CODE])
        assertEquals(11L, span.attributes[HttpAttributes.HTTP_REQUEST_BODY_SIZE])
    }

    @Test
    fun testErrorStatus() = runTest {
        var inMemoryExporter: InMemorySpanExporter? = null
        val otel = createOpenTelemetry {
            tracerProvider {
                export {
                    val exporter = inMemorySpanExporter()
                    inMemoryExporter = exporter
                    simpleSpanProcessor(exporter)
                }
            }
        }

        val client = HttpClient(MockEngine) {
            install(OpenTelemetryKtor) {
                openTelemetry = otel
            }
            engine {
                addHandler { _ ->
                    respond("Not Found", HttpStatusCode.NotFound)
                }
            }
        }

        client.get("https://example.com/404")

        val spans = inMemoryExporter?.exportedSpans ?: emptyList()
        assertEquals(1, spans.size)
        val span = spans[0]

        assertEquals(404L, span.attributes[HttpAttributes.HTTP_RESPONSE_STATUS_CODE])
        assertEquals(StatusCode.ERROR, span.status.statusCode)
    }

    @Test
    fun testExceptionHandling() = runTest {
        var inMemoryExporter: InMemorySpanExporter? = null
        val otel = createOpenTelemetry {
            tracerProvider {
                export {
                    val exporter = inMemorySpanExporter()
                    inMemoryExporter = exporter
                    simpleSpanProcessor(exporter)
                }
            }
        }

        val client = HttpClient(MockEngine) {
            install(OpenTelemetryKtor) {
                openTelemetry = otel
            }
            engine {
                addHandler { _ ->
                    throw IllegalStateException("Network failure")
                }
            }
        }

        try {
            client.get("https://example.com/fail")
        } catch (e: Exception) {
            // expected
        }

        val spans = inMemoryExporter?.exportedSpans ?: emptyList()
        assertEquals(1, spans.size)
        val span = spans[0]

        assertEquals(StatusCode.ERROR, span.status.statusCode)
        assertEquals("Network failure", span.status.description)
        
        val event = span.events.find { it.name == "exception" }
        assertNotNull(event)
        assertEquals("Network failure", event.attributes["exception.message"])
        assertTrue(event.attributes.containsKey("exception.stacktrace"))
    }
}
