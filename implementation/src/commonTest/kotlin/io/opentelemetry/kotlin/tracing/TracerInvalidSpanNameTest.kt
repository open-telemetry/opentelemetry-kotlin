package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TracerInvalidSpanNameTest {

    private val key = InstrumentationScopeInfoImpl("test-tracer", null, null, emptyMap())
    private lateinit var clock: FakeClock
    private lateinit var idGenerator: IdGenerator
    private lateinit var processor: FakeSpanProcessor
    private lateinit var errorHandler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        idGenerator = IdGeneratorImpl()
        processor = FakeSpanProcessor()
        errorHandler = FakeSdkErrorHandler()
    }

    @Test
    fun testEmptyNameReturnsNonRecordingSpan() {
        val span = createTracer().startSpan("")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.isValid)
        assertTrue(processor.startCalls.isEmpty())
        assertEquals(1, errorHandler.apiMisuses.size)
        assertEquals("Tracer.startSpan", errorHandler.apiMisuses.single().api)
    }

    @Test
    fun testBlankNameReturnsNonRecordingSpan() {
        val span = createTracer().startSpan("   ")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.isValid)
        assertTrue(processor.startCalls.isEmpty())
        assertEquals(1, errorHandler.apiMisuses.size)
    }

    @Test
    fun testBlankNameKeepsParentSpanContext() {
        val tracer = createTracer()
        val parent = tracer.startSpan("parent")
        val ctx = rootContext().storeSpan(parent)
        val span = tracer.startSpan("  ", parentContext = ctx)
        assertFalse(span.isRecording())
        assertEquals(parent.spanContext, span.spanContext)
        assertEquals(1, errorHandler.apiMisuses.size)
    }

    @Test
    fun testSetBlankNameIsIgnored() {
        val span = createTracer().startSpan("original")
        span.setName("")
        span.setName("  ")
        span.end()
        assertEquals("original", processor.endCalls.single().name)
        assertEquals(2, errorHandler.apiMisuses.size)
        assertEquals("Span.setName", errorHandler.apiMisuses.first().api)
    }

    private fun createTracer() = TracerImpl(
        clock = clock,
        processor = processor,
        scope = key,
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = idGenerator,
        shutdownState = MutableShutdownState(),
        sdkErrorHandler = errorHandler,
    )
}
