package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.factory.createSdkFactory
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.hex
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TracerSpanContextTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor
    private lateinit var sdkFactory: SdkFactory

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        sdkFactory = createSdkFactory()
        tracer = TracerImpl(
            clock,
            processor,
            sdkFactory,
            key,
            FakeResource(),
            fakeSpanLimitsConfig,
        )
    }

    @Test
    fun testNoExplicitParentContext() {
        val span = tracer.createSpan("test")
        assertFalse(span.parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
    }

    @Test
    fun testExplicitParentContextOfInvalidSpan() {
        val invalidSpan = sdkFactory.spanFactory.invalid
        assertFalse(invalidSpan.spanContext.isValid)
        val parentCtx = sdkFactory.contextFactory.storeSpan(sdkFactory.contextFactory.root(), invalidSpan)
        val span = tracer.createSpan("test", parentContext = parentCtx)

        assertFalse(span.parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
    }

    @Test
    fun testExplicitParentContextOfValidSpan() {
        val parentSpan = tracer.createSpan("parent")
        val parentCtx = sdkFactory.contextFactory.storeSpan(sdkFactory.contextFactory.root(), parentSpan)
        val span = tracer.createSpan("test", parentContext = parentCtx)

        assertTrue(span.parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
        assertEquals(parentSpan.spanContext.traceId, spanContext.traceId)
        assertNotEquals(parentSpan.spanContext.spanId, spanContext.spanId)
    }

    @Test
    fun testImplicitContext() {
        val span = tracer.createSpan("span")
        val ctx = sdkFactory.contextFactory.storeSpan(sdkFactory.contextFactory.root(), span)
        val scope = ctx.attach()

        val first = tracer.createSpan("first")
        first.end()

        scope.detach()

        val second = tracer.createSpan("second")
        second.end()

        assertSame(span.spanContext, first.parent)
        assertSame(sdkFactory.spanContextFactory.invalid, second.parent)
    }

    private fun assertValidSpanContext(spanContext: SpanContext) {
        assertTrue(spanContext.isValid)
        assertFalse(spanContext.isRemote)
        assertNotEquals(sdkFactory.tracingIdFactory.invalidTraceId.toHexString(), spanContext.traceId)
        assertNotEquals(sdkFactory.tracingIdFactory.invalidSpanId.toHexString(), spanContext.spanId)
        assertEquals(emptyMap(), spanContext.traceState.asMap())
        assertEquals("01", spanContext.traceFlags.hex)
    }
}
