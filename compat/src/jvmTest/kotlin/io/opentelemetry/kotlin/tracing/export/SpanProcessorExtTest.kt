package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaExtendedSpanProcessor
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanProcessor
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SpanProcessorExtTest {

    @Test
    fun toOtelKotlinSpanProcessor() = runTest {
        val impl = FakeOtelJavaSpanProcessor()
        val adapter = impl.toOtelKotlinSpanProcessor()
        val harness = OtelKotlinHarness(testScheduler)
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
        assertFalse(adapter.isOnEndingRequired())
    }

    @Test
    fun testExtendedProcessorIsRequired() {
        val impl = FakeOtelJavaExtendedSpanProcessor(onEndingRequired = false)
        assertFalse(impl.toOtelKotlinSpanProcessor().isOnEndingRequired())
        assertTrue(FakeOtelJavaExtendedSpanProcessor().toOtelKotlinSpanProcessor().isOnEndingRequired())
    }

    @Test
    fun toOtelKotlinExtendedSpanProcessor() = runTest {
        val impl = FakeOtelJavaExtendedSpanProcessor()
        val harness = OtelKotlinHarness(testScheduler)
        harness.config.spanProcessors.add(impl.toOtelKotlinSpanProcessor())

        val tracer = harness.javaApi.tracerProvider.get("tracer")
        val spanName = "my_span"
        tracer.spanBuilder(spanName).startSpan().end()

        assertSame(spanName, impl.startCalls.single().name)
        assertSame(spanName, impl.endingCalls.single().name)
        assertSame(spanName, impl.endCalls.single().name)
    }

    @Test
    fun testFlush() = runTest {
        val impl = FakeOtelJavaSpanProcessor()
        val adapter = impl.toOtelKotlinSpanProcessor()
        adapter.forceFlush()
        assertEquals(1, impl.flushCount)
    }

    @Test
    fun testShutdown() = runTest {
        val impl = FakeOtelJavaSpanProcessor()
        val adapter = impl.toOtelKotlinSpanProcessor()
        adapter.shutdown()
        assertEquals(1, impl.shutdownCount)
    }
}
