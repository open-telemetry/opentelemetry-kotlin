package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.toOtelJavaApi
import io.opentelemetry.kotlin.toOtelKotlinApi
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Asserts that the Kotlin and Java Context APIs share a single implicit context in compat mode.
 */
internal class CompatImplicitContextIntegrationTest {

    private val errorHandler = FakeSdkErrorHandler()
    private val otel = createCompatOpenTelemetry {
        errorHandler(errorHandler)
        tracerProvider { export { FakeSpanProcessor() } }
    }
    private val tracer = otel.tracerProvider.getTracer("kotlin")
    private val javaTracer = otel.toOtelJavaApi().tracerProvider.get("java")

    @After
    fun resetImplicitContext() {
        try {
            assertEquals(OtelJavaContext.root(), OtelJavaContext.current(), "Test leaked a scope")
            assertFalse(errorHandler.hasErrors())
        } finally {
            OtelJavaContext.root().makeCurrent()
        }
    }

    @Test
    fun `kotlin context value is visible to java`() {
        val key = createContextKey<String>("key")
        val scope = otel.context.current().set(key, "value").attach()
        try {
            assertEquals("value", OtelJavaContext.current().get(key.toOtelJavaContextKey()))
        } finally {
            scope.detach()
        }
        assertEquals(OtelJavaContext.root(), OtelJavaContext.current())
    }

    @Test
    fun `java context value is visible to kotlin`() {
        val key = OtelJavaContextKey.named<String>("key")
        OtelJavaContext.current().with(key, "value").makeCurrent().use {
            assertEquals("value", otel.context.current().toOtelJavaContext().get(key))
        }
        assertEquals(otel.context.root(), otel.context.current())
    }

    @Test
    fun `kotlin span is current in java`() {
        val span = tracer.startSpan("kotlin")
        val scope = otel.context.current().storeSpan(span).attach()
        try {
            val javaSpan = OtelJavaSpan.current()
            assertEquals(span.spanContext.traceId, javaSpan.spanContext.traceId)
            assertEquals(span.spanContext.spanId, javaSpan.spanContext.spanId)
        } finally {
            scope.detach()
            span.end()
        }
        assertFalse(OtelJavaSpan.current().spanContext.isValid)
    }

    @Test
    fun `java span is parent of kotlin span`() {
        val parent = javaTracer.spanBuilder("java").startSpan()
        val child = try {
            parent.makeCurrent().use {
                assertEquals(parent.spanContext.spanId, otel.context.current().extractSpan().spanContext.spanId)
                tracer.startSpan("kotlin")
            }
        } finally {
            parent.end()
        }
        assertTrue(child.parent.isValid)
        assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
        assertEquals(parent.spanContext.spanId, child.parent.spanId)
    }

    @Test
    fun `kotlin span is parent of java span`() {
        val parent = tracer.startSpan("kotlin")
        val scope = otel.context.current().storeSpan(parent).attach()
        val child = try {
            javaTracer.spanBuilder("java").startSpan()
        } finally {
            scope.detach()
            parent.end()
        }
        assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
        assertEquals(parent.spanContext.spanId, (child as OtelJavaSpanAdapter).span.parent.spanId)
    }

    @Test
    fun `kotlin baggage is visible to java`() {
        val baggage = createBaggage { put("key", "value") }
        val scope = otel.context.current().storeBaggage(baggage).attach()
        try {
            assertEquals("value", OtelJavaBaggage.current().getEntryValue("key"))
        } finally {
            scope.detach()
        }
        assertTrue(OtelJavaBaggage.current().isEmpty)
    }

    @Test
    fun `java baggage is visible to kotlin`() {
        OtelJavaBaggage.builder().put("key", "value").build().makeCurrent().use {
            assertEquals("value", otel.context.current().extractBaggage().getValue("key"))
        }
        assertTrue(otel.context.current().extractBaggage().asMap().isEmpty())
    }

    @Test
    fun `nested scopes restore across apis`() {
        val kotlinKey = createContextKey<String>("kotlin")
        val javaKey = OtelJavaContextKey.named<String>("java")

        val outer = otel.context.current().set(kotlinKey, "outer").attach()
        try {
            OtelJavaContext.current().with(javaKey, "inner").makeCurrent().use {
                val ctx = otel.context.current()
                assertEquals("outer", ctx.get(kotlinKey))
                assertEquals("inner", ctx.toOtelJavaContext().get(javaKey))
            }
            assertEquals("outer", OtelJavaContext.current().get(kotlinKey.toOtelJavaContextKey()))
            assertEquals(null, OtelJavaContext.current().get(javaKey))
        } finally {
            outer.detach()
        }
        assertEquals(OtelJavaContext.root(), OtelJavaContext.current())
    }

    @Test
    fun `toOtelKotlinApi shares java implicit context`() {
        val javaSdk = OtelJavaOpenTelemetrySdk.builder()
            .setTracerProvider(OtelJavaSdkTracerProvider.builder().build())
            .build()
        val kotlinApi: OpenTelemetry = javaSdk.toOtelKotlinApi()
        val parent = javaSdk.getTracer("java").spanBuilder("java").startSpan()
        val child = try {
            parent.makeCurrent().use {
                kotlinApi.tracerProvider.getTracer("kotlin").startSpan("kotlin")
            }
        } finally {
            parent.end()
        }
        assertEquals(parent.spanContext.spanId, child.parent.spanId)
    }

    @Test
    fun `context configuration is ignored with a warning`() {
        val handler = FakeSdkErrorHandler()
        val sdk = createCompatOpenTelemetry {
            errorHandler(handler)
            context { storageMode = ImplicitContextStorageMode.GLOBAL }
        }
        assertEquals(1, handler.apiMisuses.size)

        val key = OtelJavaContextKey.named<String>("key")
        OtelJavaContext.current().with(key, "value").makeCurrent().use {
            assertEquals("value", sdk.context.current().toOtelJavaContext().get(key))
        }
    }
}
