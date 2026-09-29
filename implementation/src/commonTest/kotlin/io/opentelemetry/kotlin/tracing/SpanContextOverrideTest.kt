package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.factory.DefaultTraceFlagsFactory
import io.opentelemetry.kotlin.factory.DefaultTraceStateFactory
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class SpanContextOverrideTest {

    private val scope = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: Tracer
    private lateinit var processor: FakeSpanProcessor
    private lateinit var spanContextFactory: SpanContextFactory
    private lateinit var traceFlagsFactory: TraceFlagsFactory
    private lateinit var traceStateFactory: TraceStateFactory
    private lateinit var ctx: SpanContext

    @BeforeTest
    fun setUp() {
        processor = FakeSpanProcessor()
        val idGenerator = IdGeneratorImpl()
        traceFlagsFactory = DefaultTraceFlagsFactory
        traceStateFactory = DefaultTraceStateFactory
        spanContextFactory = DefaultSpanContextFactory
        val spanFactory = SpanFactoryImpl(spanContextFactory)
        val contextFactory = ContextFactoryImpl(spanFactory)
        tracer = TracerImpl(
            clock = FakeClock(),
            processor = processor,
            contextFactory = contextFactory,
            spanContextFactory = spanContextFactory,
            scope = scope,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = idGenerator,
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = NoopSdkErrorHandler,
        )
        ctx = spanContextFactory.create(
            traceId = "0af7651916cd43dd8448eb211c80319c",
            spanId = "b7ad6b7169203331",
            traceFlags = DefaultTraceFlagsFactory.create(isSampled = true, isRandom = false),
            traceState = traceStateFactory.default,
            isRemote = false,
        )
    }

    @Test
    fun testSpanContextOverride() {
        assertTrue(ctx.isValid)
        processor.startAction = { span, _ -> span.spanContext = ctx }
        val span = tracer.startSpan("test")

        assertEquals(ctx.traceId, span.spanContext.traceId)
        assertEquals(ctx.spanId, span.spanContext.spanId)
    }

    @Test
    fun testSpanContextOverrideAfterEnd() {
        val span = tracer.startSpan("test")
        span.end()
        val readWriteSpan = processor.endingCalls.single()
        val originalTraceId = readWriteSpan.spanContext.traceId
        val originalSpanId = readWriteSpan.spanContext.spanId

        assertNotEquals(originalTraceId, ctx.traceId)
        readWriteSpan.spanContext = ctx
        assertEquals(originalTraceId, readWriteSpan.spanContext.traceId)
        assertEquals(originalSpanId, readWriteSpan.spanContext.spanId)
    }
}
