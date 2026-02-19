package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.TracerImpl
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import io.opentelemetry.kotlin.tracing.fakeSpanLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class LogContextTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var logger: LoggerImpl
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeLogRecordProcessor
    private lateinit var contextFactory: ContextFactory
    private lateinit var spanContextFactory: SpanContextFactory
    private lateinit var spanFactory: SpanFactory

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeLogRecordProcessor()
        val idGenerator = IdGeneratorImpl()
        val traceFlags = TraceFlagsFactoryImpl()
        val traceState = TraceStateFactoryImpl()
        spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlags, traceState)
        contextFactory = ContextFactoryImpl()
        spanFactory =
            SpanFactoryImpl(spanContextFactory, (contextFactory as ContextFactoryImpl).spanKey)
        logger = LoggerImpl(
            clock,
            processor,
            contextFactory,
            spanContextFactory,
            spanFactory,
            key,
            FakeResource(),
            fakeLogLimitsConfig
        )
        tracer = TracerImpl(
            clock = clock,
            processor = FakeSpanProcessor(),
            contextFactory = contextFactory,
            spanContextFactory = spanContextFactory,
            traceFlagsFactory = traceFlags,
            traceStateFactory = traceState,
            spanFactory = spanFactory,
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = FakeIdGenerator(),
            shutdownState = MutableShutdownState(),
        )
    }

    @Test
    fun testDefaultContext() {
        logger.emit()
        val log = processor.logs.single()
        val root = spanFactory.fromContext(contextFactory.root()).spanContext
        assertSame(root, log.spanContext)
    }

    @Test
    fun testOverrideContext() {
        val span = tracer.startSpan("span")
        val ctx = contextFactory.storeSpan(contextFactory.root(), span)
        logger.emit(
            context = ctx,
        )

        val log = processor.logs.single()
        assertSame(span.spanContext, log.spanContext)
    }

    @Test
    fun testImplicitContext() {
        val span = tracer.startSpan("span")
        val ctx = contextFactory.storeSpan(contextFactory.root(), span)
        val scope = ctx.attach()
        logger.emit()

        scope.detach()
        logger.emit()

        assertEquals(2, processor.logs.size)
        assertSame(span.spanContext, processor.logs[0].spanContext)
        assertSame(spanContextFactory.invalid, processor.logs[1].spanContext)
    }
}
