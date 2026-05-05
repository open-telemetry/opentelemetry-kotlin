package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
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
    private lateinit var contextFactory: ContextFactory
    private lateinit var spanContextFactory: SpanContextFactory
    private lateinit var spanFactory: SpanFactory
    private lateinit var idGenerator: IdGenerator

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        idGenerator = IdGeneratorImpl()
        val traceFlags = TraceFlagsFactoryImpl()
        val traceState = TraceStateFactoryImpl()
        spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlags, traceState)
        spanFactory = SpanFactoryImpl(spanContextFactory)
        contextFactory = ContextFactoryImpl(spanFactory)
        tracer = TracerImpl(
            clock = clock,
            processor = processor,
            contextFactory = contextFactory,
            spanContextFactory = spanContextFactory,
            traceFlagsFactory = traceFlags,
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = idGenerator,
            shutdownState = MutableShutdownState(),
        )
    }

    @Test
    fun testNoExplicitParentContext() {
        val span = tracer.startSpan("test")
        assertFalse((span.toReadableSpan()).parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
    }

    @Test
    fun testExplicitParentContextOfInvalidSpan() {
        val invalidSpan = spanFactory.invalid
        assertFalse(invalidSpan.spanContext.isValid)
        val parentCtx = contextFactory.root().storeSpan(invalidSpan)
        val span = tracer.startSpan(
            "test",
            parentContext = parentCtx,
        )

        assertFalse((span.toReadableSpan()).parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
    }

    @Test
    fun testExplicitParentContextOfValidSpan() {
        val parentSpan = tracer.startSpan("parent")
        val parentCtx = contextFactory.root().storeSpan(parentSpan)
        val span = tracer.startSpan(
            "test",
            parentContext = parentCtx,
        )

        assertTrue((span.toReadableSpan()).parent.isValid)
        val spanContext = span.spanContext
        assertValidSpanContext(spanContext)
        assertEquals(parentSpan.spanContext.traceId, spanContext.traceId)
        assertNotEquals(parentSpan.spanContext.spanId, spanContext.spanId)
    }

    @Test
    fun testImplicitContext() {
        val span = tracer.startSpan("span")
        val ctx = contextFactory.root().storeSpan(span)
        val scope = ctx.attach()

        val first = tracer.startSpan("first")
        first.end()

        scope.detach()

        val second = tracer.startSpan("second")
        second.end()

        assertSame(span.spanContext, first.toReadableSpan().parent)
        assertSame(spanContextFactory.invalid, second.toReadableSpan().parent)
    }

    private fun assertValidSpanContext(spanContext: SpanContext) {
        assertTrue(spanContext.isValid)
        assertFalse(spanContext.isRemote)
        assertNotEquals(idGenerator.invalidTraceId.toHexString(), spanContext.traceId)
        assertNotEquals(idGenerator.invalidSpanId.toHexString(), spanContext.spanId)
        assertEquals(emptyMap(), spanContext.traceState.asMap())
        assertEquals("01", spanContext.traceFlags.hex)
    }
}
