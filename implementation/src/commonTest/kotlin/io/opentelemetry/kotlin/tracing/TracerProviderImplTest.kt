package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.factory.FakeTraceStateFactory
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.ResourceImpl
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

internal class TracerProviderImplTest {

    private val tracingConfig = TracingConfig(
        emptyList(),
        fakeSpanLimitsConfig,
        ResourceImpl(AttributesModel(), null)
    )

    private lateinit var impl: TracerProviderImpl

    @BeforeTest
    fun setUp() {
        impl = TracerProviderImpl(
            clock = FakeClock(),
            tracingConfig = tracingConfig,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            traceStateFactory = FakeTraceStateFactory(),
            spanFactory = FakeSpanFactory(),
            idGenerator = FakeIdGenerator(),
        )
    }

    @Test
    fun testMinimalTracerProvider() {
        assertNotNull(impl.getTracer(name = ""))
    }

    @Test
    fun testFullTracerProvider() {
        val first = impl.getTracer(
            name = "name",
            version = "0.1.0",
            schemaUrl = "https://example.com/foo"
        ) {
            setStringAttribute("key", "value")
        }
        assertNotNull(first)
    }

    @Test
    fun testTracerProviderSameName() {
        val first = impl.getTracer(name = "name")
        val second = impl.getTracer(name = "name")
        val third = impl.getTracer(name = "other")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testTracerProviderSameVersion() {
        val first = impl.getTracer(name = "name", version = "0.1.0")
        val second = impl.getTracer(name = "name", version = "0.1.0")
        val third = impl.getTracer(name = "name", version = "0.2.0")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testTracerProviderSameSchemaUrl() {
        val first = impl.getTracer(name = "name", schemaUrl = "https://example.com/foo")
        val second = impl.getTracer(name = "name", schemaUrl = "https://example.com/foo")
        val third = impl.getTracer(name = "name", schemaUrl = "https://example.com/bar")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testTracerProviderSameAttributes() {
        val first = impl.getTracer(name = "name") {
            setStringAttribute("key", "value")
        }
        val second = impl.getTracer(name = "name") {
            setStringAttribute("key", "value")
        }
        val third = impl.getTracer(name = "name") {
            setStringAttribute("foo", "bar")
        }
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testForceFlushEmptyProcessors() = runTest {
        val result = impl.forceFlush()
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testShutdownEmptyProcessors() = runTest {
        val result = impl.shutdown()
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testForceFlushProcessorDelegation() = runTest {
        var flushCalled = false
        val processor = FakeSpanProcessor(
            flushCode = {
                flushCalled = true
                OperationResultCode.Success
            }
        )
        val config = TracingConfig(
            listOf(processor),
            fakeSpanLimitsConfig,
            FakeResource(),
        )
        val provider = TracerProviderImpl(
            clock = FakeClock(),
            tracingConfig = config,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            traceStateFactory = FakeTraceStateFactory(),
            spanFactory = FakeSpanFactory(),
            idGenerator = FakeIdGenerator(),
        )
        provider.getTracer(name = "test")

        val result = provider.forceFlush()
        assertEquals(OperationResultCode.Success, result)
        assertEquals(true, flushCalled)
    }

    @Test
    fun testShutdownProcessorDelegation() = runTest {
        var shutdownCalled = false
        val processor = FakeSpanProcessor(
            shutdownCode = {
                shutdownCalled = true
                OperationResultCode.Success
            }
        )
        val config = TracingConfig(
            listOf(processor),
            fakeSpanLimitsConfig,
            FakeResource(),
        )
        val provider = TracerProviderImpl(
            clock = FakeClock(),
            tracingConfig = config,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            traceStateFactory = FakeTraceStateFactory(),
            spanFactory = FakeSpanFactory(),
            idGenerator = FakeIdGenerator(),
        )
        provider.getTracer(name = "test")

        val result = provider.shutdown()
        assertEquals(OperationResultCode.Success, result)
        assertEquals(true, shutdownCalled)
    }

    @Test
    fun testGetTracerAfterShutdownReturnsNoopTracer() = runTest {
        impl.shutdown()
        val tracer = impl.getTracer(name = "test")
        val span = tracer.startSpan("test-span")
        assertFalse(span.isRecording())
    }

    @Test
    fun testExistingTracerReturnsNoopSpanAfterShutdown() = runTest {
        val tracer = impl.getTracer(name = "test")
        impl.shutdown()
        val span = tracer.startSpan("test-span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.isValid)
    }
}
