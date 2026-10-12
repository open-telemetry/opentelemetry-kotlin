package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TracerInvalidSpanNameTest {

    private val key = InstrumentationScopeInfoImpl("test-tracer", null, null, emptyMap())
    private lateinit var clock: FakeClock
    private lateinit var contextFactory: ContextFactory
    private lateinit var idGenerator: IdGenerator
    private lateinit var processor: FakeSpanProcessor

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        idGenerator = IdGeneratorImpl()
        contextFactory = ContextFactoryImpl(SpanFactoryImpl())
        processor = FakeSpanProcessor()
    }

    @Test
    fun testEmptyNameReturnsNonRecordingSpan() {
        val span = createTracer().startSpan("")
        assertTrue(span is NonRecordingSpan)
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.isValid)
        assertTrue(processor.startCalls.isEmpty())
    }

    @Test
    fun testBlankNameReturnsNonRecordingSpan() {
        val span = createTracer().startSpan("   ")
        assertTrue(span is NonRecordingSpan)
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.isValid)
        assertTrue(processor.startCalls.isEmpty())
    }

    private fun createTracer() = TracerImpl(
        clock = clock,
        processor = processor,
        contextFactory = contextFactory,
        scope = key,
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = idGenerator,
        shutdownState = MutableShutdownState(),
        sdkErrorHandler = NoopSdkErrorHandler,
    )
}
