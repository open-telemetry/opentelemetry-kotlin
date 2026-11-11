package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TracerEnabledTest {

    private val key = InstrumentationScopeInfoImpl("test-tracer", null, null, emptyMap())
    private lateinit var clock: FakeClock
    private lateinit var idGenerator: IdGenerator

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        idGenerator = IdGeneratorImpl()
    }

    @Test
    fun testNoProcessor() {
        val tracer = createTracer(processor = null)
        assertFalse(tracer.enabled())
    }

    @Test
    fun testWithProcessor() {
        val tracer = createTracer(processor = FakeSpanProcessor())
        assertTrue(tracer.enabled())
    }

    @Test
    fun testShutdown() {
        val shutdownState = MutableShutdownState()
        val tracer = createTracer(processor = FakeSpanProcessor(), shutdownState = shutdownState)
        assertTrue(tracer.enabled())
        shutdownState.shutdownNow()
        assertFalse(tracer.enabled())
    }

    private fun createTracer(
        processor: SpanProcessor?,
        shutdownState: MutableShutdownState = MutableShutdownState(),
    ) = TracerImpl(
        clock = clock,
        processor = processor,
        scope = key,
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = idGenerator,
        shutdownState = shutdownState,
        sdkErrorHandler = NoopSdkErrorHandler,
    )
}
