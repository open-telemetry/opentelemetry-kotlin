package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.factory.createSdkFactory
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

@OptIn(ExperimentalApi::class)
internal class LogContextTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var logger: LoggerImpl
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeLogRecordProcessor
    private lateinit var sdkFactory: SdkFactory

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeLogRecordProcessor()
        sdkFactory = createSdkFactory()
        logger = LoggerImpl(
            clock,
            processor,
            sdkFactory,
            key,
            FakeResource(),
            fakeLogLimitsConfig
        )
        tracer = TracerImpl(
            clock,
            FakeSpanProcessor(),
            sdkFactory,
            key,
            FakeResource(),
            fakeSpanLimitsConfig
        )
    }

    @Test
    fun testDefaultContext() {
        logger.emit()
        val log = processor.logs.single()
        val root = sdkFactory.spanFactory.fromContext(sdkFactory.contextFactory.root()).spanContext
        assertSame(root, log.spanContext)
    }

    @Test
    fun testOverrideContext() {
        val span = tracer.startSpan("span")
        val ctx = sdkFactory.contextFactory.storeSpan(sdkFactory.contextFactory.root(), span)
        logger.emit(
            context = ctx,
        )

        val log = processor.logs.single()
        assertSame(span.spanContext, log.spanContext)
    }

    @Test
    fun testImplicitContext() {
        val span = tracer.startSpan("span")
        val ctx = sdkFactory.contextFactory.storeSpan(sdkFactory.contextFactory.root(), span)
        val scope = ctx.attach()
        logger.emit()

        scope.detach()
        logger.emit()

        assertEquals(2, processor.logs.size)
        assertSame(span.spanContext, processor.logs[0].spanContext)
        assertSame(sdkFactory.spanContextFactory.invalid, processor.logs[1].spanContext)
    }
}
