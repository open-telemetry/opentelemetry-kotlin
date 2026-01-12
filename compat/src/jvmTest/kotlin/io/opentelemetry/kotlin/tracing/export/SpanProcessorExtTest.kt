package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanProcessor
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanProcessorExtTest {

    @Test
    fun toOtelKotlinSpanProcessor() {
        val impl = FakeOtelJavaSpanProcessor()
        val adapter = impl.toOtelKotlinSpanProcessor()
        val harness = OtelKotlinHarness()
        harness.config.spanProcessors.add(adapter)

        val tracer = harness.javaApi.tracerProvider.get("tracer")
        val spanName = "my_span"
        tracer.spanBuilder(spanName).startSpan().end()

        assertSame(spanName, impl.startCalls.single().name)
        assertSame(spanName, impl.endCalls.single().name)
    }

    @Test
    fun testIsRequired() {
        val impl = FakeOtelJavaSpanProcessor()
        val adapter = impl.toOtelKotlinSpanProcessor()
        assertTrue(adapter.isStartRequired())
        assertTrue(adapter.isEndRequired())
    }

    @Test
    fun testFlush() {
        val impl = FakeOtelJavaSpanProcessor()
        val adapter = impl.toOtelKotlinSpanProcessor()
        adapter.forceFlush()
        assertEquals(1, impl.flushCount)
    }

    @Test
    fun testShutdown() {
        val impl = FakeOtelJavaSpanProcessor()
        val adapter = impl.toOtelKotlinSpanProcessor()
        adapter.shutdown()
        assertEquals(1, impl.shutdownCount)
    }
}
