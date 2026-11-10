package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.internalInstallContextStorage
import io.opentelemetry.kotlin.context.internalRootContext
import io.opentelemetry.kotlin.context.internalUninstallContextStorage
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.TracerImpl
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import io.opentelemetry.kotlin.tracing.fakeSpanLimitsConfig
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(TempContextStandInApi::class)
internal class LogContextTest {

    private val storage = createSingleSlotCurrentContextStorage()

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var logger: LoggerImpl
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeLogRecordProcessor

    @BeforeTest
    fun setUp() {
        internalInstallContextStorage(storage)
        clock = FakeClock()
        processor = FakeLogRecordProcessor()
        logger = LoggerImpl(
            clock,
            processor,
            key,
            FakeResource(),
            fakeLogLimitsConfig,
            MutableShutdownState(),
            sdkErrorHandler = NoopSdkErrorHandler,
        )
        tracer = TracerImpl(
            clock = clock,
            processor = FakeSpanProcessor(),
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = FakeIdGenerator(),
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = NoopSdkErrorHandler,
        )
    }

    @AfterTest
    fun tearDown() {
        internalUninstallContextStorage(storage)
    }

    @Test
    fun testDefaultContext() {
        logger.emit()
        val log = processor.logs.single()
        assertSame(createInvalidSpanContext(), log.spanContext)
    }

    @Test
    fun testOverrideContext() {
        val span = tracer.startSpan("span")
        val ctx = internalRootContext().storeSpan(span)
        logger.emit(
            context = ctx,
        )

        val log = processor.logs.single()
        assertSame(span.spanContext, log.spanContext)
    }

    @Test
    fun testCurrentContext() {
        val span = tracer.startSpan("span")
        val ctx = internalRootContext().storeSpan(span)
        val scope = ctx.attach()
        logger.emit()

        scope.detach()
        logger.emit()

        assertEquals(2, processor.logs.size)
        assertSame(span.spanContext, processor.logs[0].spanContext)
        assertSame(createInvalidSpanContext(), processor.logs[1].spanContext)
    }
}
