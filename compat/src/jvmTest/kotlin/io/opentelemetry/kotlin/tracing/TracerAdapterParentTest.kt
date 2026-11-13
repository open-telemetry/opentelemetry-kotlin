package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanProcessor
import io.opentelemetry.kotlin.framework.SdkTracker
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class TracerAdapterParentTest {

    private val javaTracerProvider = OtelJavaSdkTracerProvider.builder()
        .addSpanProcessor(FakeOtelJavaSpanProcessor())
        .build()
    private val tracer = TracerProviderAdapter(
        javaTracerProvider,
        NoopSdkErrorHandler,
    ).getTracer("test")

    private val sdks = SdkTracker().also { createCompatOpenTelemetry().also(it::track) }

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `kotlin span inherits active java span as parent`() {
        val parent = javaTracerProvider.get("java").spanBuilder("parent").startSpan()
        val scope = parent.makeCurrent()
        val child = try {
            tracer.startSpan("child")
        } finally {
            scope.close()
            parent.end()
        }

        assertTrue(child.parent.isValid)
        assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
        assertEquals(parent.spanContext.spanId, child.parent.spanId)
    }

    @Test
    fun `kotlin span inherits attached kotlin span as parent`() {
        val parent = tracer.startSpan("parent")
        val scope = currentContext().storeSpan(parent).attach()
        val child = try {
            tracer.startSpan("child")
        } finally {
            scope.detach()
            parent.end()
        }

        assertTrue(child.parent.isValid)
        assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
        assertEquals(parent.spanContext.spanId, child.parent.spanId)
    }

    @Test
    fun `explicit root context starts a new trace`() {
        val parent = javaTracerProvider.get("java").spanBuilder("parent").startSpan()
        val scope = parent.makeCurrent()
        val child = try {
            tracer.startSpan("child", parentContext = rootContext())
        } finally {
            scope.close()
            parent.end()
        }

        assertFalse(child.parent.isValid)
        assertNotEquals(parent.spanContext.traceId, child.spanContext.traceId)
    }
}
