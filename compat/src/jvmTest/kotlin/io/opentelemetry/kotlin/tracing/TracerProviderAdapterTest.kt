package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanProcessor
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class TracerProviderAdapterTest {

    private val adapter = TracerProviderAdapter(
        OtelJavaSdkTracerProvider.builder().build(),
        CompatSpanLimitsConfig(),
        CompatContextFactory(),
        NoopSdkErrorHandler,
    )

    @Test
    fun testDupeTracerProviderAttributes() {
        val first = adapter.getTracer(name = "name") {
            setStringAttribute("key", "value")
        }
        val second = adapter.getTracer(name = "name") {
            setStringAttribute("key", "value")
        }
        val third = adapter.getTracer(name = "name") {
            setStringAttribute("foo", "bar")
        }
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testScopePropertyBoundaryCollision() {
        val first = adapter.getTracer(name = "ab", version = "c")
        val second = adapter.getTracer(name = "a", version = "bc")
        assertNotSame(first, second)
    }

    @Test
    fun testNullScopePropertyCollision() {
        val first = adapter.getTracer(name = "name")
        val second = adapter.getTracer(name = "name", version = "null")
        assertNotSame(first, second)
    }

    @Test
    fun testForceFlushAndShutdownDelegateToJavaSdkProvider() = runTest {
        val processor = FakeOtelJavaSpanProcessor()
        val provider = OtelJavaSdkTracerProvider.builder()
            .addSpanProcessor(processor)
            .build()
        val adapter = TracerProviderAdapter(
            provider,
            CompatSpanLimitsConfig(),
            CompatContextFactory(),
            NoopSdkErrorHandler,
        )

        assertEquals(OperationResultCode.Success, adapter.forceFlush())
        assertEquals(1, processor.flushCount)
        assertEquals(OperationResultCode.Success, adapter.shutdown())
        assertEquals(1, processor.shutdownCount)
    }
}
