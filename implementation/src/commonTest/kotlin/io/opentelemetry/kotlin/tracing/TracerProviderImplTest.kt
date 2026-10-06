package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.init.SamplerConfigImpl
import io.opentelemetry.kotlin.init.config.DefaultSampler
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.ResourceImpl
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class TracerProviderImplTest {

    private val tracingConfig = TracingConfig(
        null,
        ResourceImpl(AttributesModel(), null),
        NoopSdkErrorHandler,
    )

    private lateinit var impl: TracerProviderImpl

    @BeforeTest
    fun setUp() {
        impl = TracerProviderImpl(
            clock = FakeClock(),
            tracingConfig = tracingConfig,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            spanFactory = FakeSpanFactory(),
            idGenerator = FakeIdGenerator(),
            attributeLimits = AttributeLimitsBehavior(),
            spanLimits = SpanLimitsBehavior(),
        )
    }

    @Test
    fun testMinimalTracerProvider() {
        assertNotNull(impl.getTracer(name = ""))
    }

    @Test
    fun testEmptyTracerNameReportsApiMisuse() {
        val handler = FakeSdkErrorHandler()
        val config = TracingConfig(
            null,
            ResourceImpl(AttributesModel(), null),
            handler,
        )
        val provider = TracerProviderImpl(
            clock = FakeClock(),
            tracingConfig = config,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            spanFactory = FakeSpanFactory(),
            idGenerator = FakeIdGenerator(),
            attributeLimits = AttributeLimitsBehavior(),
            spanLimits = SpanLimitsBehavior(),
        )
        provider.getTracer(name = "")
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("TracerProvider.getTracer", handler.apiMisuses.single().api)
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
            processor,
            FakeResource(),
            NoopSdkErrorHandler,
        )
        val provider = TracerProviderImpl(
            clock = FakeClock(),
            tracingConfig = config,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            spanFactory = FakeSpanFactory(),
            idGenerator = FakeIdGenerator(),
            attributeLimits = AttributeLimitsBehavior(),
            spanLimits = SpanLimitsBehavior(),
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
            processor,
            FakeResource(),
            NoopSdkErrorHandler,
        )
        val provider = TracerProviderImpl(
            clock = FakeClock(),
            tracingConfig = config,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            spanFactory = FakeSpanFactory(),
            idGenerator = FakeIdGenerator(),
            attributeLimits = AttributeLimitsBehavior(),
            spanLimits = SpanLimitsBehavior(),
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

    @Test
    fun testThrowingAttributesReturnsNoopTracer() {
        val errorHandler = FakeSdkErrorHandler()
        val provider = createProvider(errorHandler = errorHandler)

        val tracer = provider.getTracer(name = "name") { error("boom") }

        assertFalse(tracer.startSpan("test-span").isRecording())
        val recorded = errorHandler.userCodeErrors.single()
        assertEquals("TracerProvider.getTracer failed", recorded.message)
        assertEquals("boom", recorded.cause.message)
    }

    @Test
    fun testThrowingSamplerFactoryFallsBackToDefaultSampler() {
        val errorHandler = FakeSdkErrorHandler()
        val provider = createProvider(errorHandler = errorHandler, samplerFactory = { error("boom") })

        assertTrue(provider.getTracer(name = "test").startSpan("test-span").isRecording())
        val recorded = errorHandler.userCodeErrors.single()
        assertEquals("Failed to create sampler, using default", recorded.message)
        assertEquals("boom", recorded.cause.message)
    }

    @Test
    fun testInvalidComposableProbabilityFallsBackToDefaultSampler() {
        val errorHandler = FakeSdkErrorHandler()
        val provider = createProvider(errorHandler = errorHandler, samplerFactory = { factory ->
            SamplerConfigImpl(factory).composite { composableProbability(1.5) }
        })

        assertTrue(provider.getTracer(name = "test").startSpan("test-span").isRecording())
        assertIs<IllegalArgumentException>(errorHandler.userCodeErrors.single().cause)
    }

    @Test
    fun testThrowingErrorHandlerDoesNotEscapeForceFlush() = runTest {
        val provider = createProvider(
            processor = FakeSpanProcessor(flushCode = { error("boom") }),
            errorHandler = ThrowingSdkErrorHandler(),
        )
        provider.getTracer(name = "test")

        assertEquals(OperationResultCode.Failure, provider.forceFlush())
    }

    @Test
    fun testThrowingErrorHandlerDoesNotEscapeShutdown() = runTest {
        val provider = createProvider(
            processor = FakeSpanProcessor(shutdownCode = { error("boom") }),
            errorHandler = ThrowingSdkErrorHandler(),
        )
        provider.getTracer(name = "test")

        assertEquals(OperationResultCode.Failure, provider.shutdown())
    }

    private fun createProvider(
        processor: SpanProcessor? = null,
        errorHandler: SdkErrorHandler,
        samplerFactory: (SpanFactory) -> Sampler = { DefaultSampler },
    ) = TracerProviderImpl(
        clock = FakeClock(),
        tracingConfig = TracingConfig(
            processor,
            FakeResource(),
            errorHandler,
            samplerFactory = samplerFactory,
        ),
        contextFactory = FakeContextFactory(),
        spanContextFactory = FakeSpanContextFactory(),
        spanFactory = FakeSpanFactory(),
        idGenerator = FakeIdGenerator(),
        attributeLimits = AttributeLimitsBehavior(),
        spanLimits = SpanLimitsBehavior(),
    )

    private class ThrowingSdkErrorHandler : SdkErrorHandler {
        override fun onError(error: SdkError): Unit = handlerBoom()
    }
}

private fun handlerBoom(): Nothing = error("handler boom")
