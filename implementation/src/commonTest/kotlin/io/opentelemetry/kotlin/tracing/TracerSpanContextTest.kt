package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.factory.SdkFactoryImpl
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
        sdkFactory = SdkFactoryImpl()
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
        val span = tracer.startSpan("test")
        assertFalse(span.parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
    }

    @Test
    fun testExplicitParentContextOfInvalidSpan() {
        val invalidSpan = sdkFactory.span.invalid
        assertFalse(invalidSpan.spanContext.isValid)
        val parentCtx = sdkFactory.context.storeSpan(sdkFactory.context.root(), invalidSpan)
        val span = tracer.startSpan(
            "test",
            parentContext = parentCtx,
        )

        assertFalse(span.parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
    }

    @Test
    fun testExplicitParentContextOfValidSpan() {
        val parentSpan = tracer.startSpan("parent")
        val parentCtx = sdkFactory.context.storeSpan(sdkFactory.context.root(), parentSpan)
        val span = tracer.startSpan(
            "test",
            parentContext = parentCtx,
        )

        assertTrue(span.parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
        assertEquals(parentSpan.spanContext.traceId, spanContext.traceId)
        assertNotEquals(parentSpan.spanContext.spanId, spanContext.spanId)
    }

    @Test
    fun testImplicitContext() {
        val span = tracer.startSpan("span")
        val ctx = sdkFactory.context.storeSpan(sdkFactory.context.root(), span)
        val scope = ctx.attach()

        val first = tracer.startSpan("first")
        first.end()

        scope.detach()

        val second = tracer.startSpan("second")
        second.end()

        assertSame(span.spanContext, first.parent)
        assertSame(sdkFactory.spanContext.invalid, second.parent)
    }

    private fun assertValidSpanContext(spanContext: SpanContext) {
        assertTrue(spanContext.isValid)
        assertFalse(spanContext.isRemote)
        assertNotEquals(sdkFactory.idGenerator.invalidTraceId.toHexString(), spanContext.traceId)
        assertNotEquals(sdkFactory.idGenerator.invalidSpanId.toHexString(), spanContext.spanId)
        assertEquals(emptyMap(), spanContext.traceState.asMap())
        assertEquals("01", spanContext.traceFlags.hex)
    }
}
