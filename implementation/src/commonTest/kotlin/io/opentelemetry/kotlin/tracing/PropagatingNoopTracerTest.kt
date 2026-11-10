package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.internalInstallContextStorage
import io.opentelemetry.kotlin.context.internalUninstallContextStorage
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, TempContextStandInApi::class)
internal class PropagatingNoopTracerTest {

    private val storage = createSingleSlotCurrentContextStorage()

    @BeforeTest
    fun setUp() {
        internalInstallContextStorage(storage)
    }

    @AfterTest
    fun tearDown() {
        internalUninstallContextStorage(storage)
    }

    private val contextFactory = ContextFactoryImpl()
    private val errorHandler = FakeSdkErrorHandler()
    private val tracer = PropagatingNoopTracer(errorHandler)
    private val contextWithSpan = contextFactory.rootContext().storeSpanContext(FakeSpanContext.VALID)

    @Test
    fun `tracer is not enabled`() {
        assertFalse(tracer.enabled())
    }

    @Test
    fun `root span has invalid span context`() {
        val span = tracer.startSpan("span", parentContext = contextFactory.rootContext())
        assertFalse(span.spanContext.isValid)
        assertFalse(span.isRecording())
    }

    @Test
    fun `span carries explicit parent span context`() {
        val span = tracer.startSpan("span", parentContext = contextWithSpan)
        assertSameSpanContext(FakeSpanContext.VALID, span.spanContext)
        assertFalse(span.isRecording())
    }

    @Test
    fun `span carries implicit parent span context`() {
        val scope = contextWithSpan.attach()
        val span = try {
            tracer.startSpan("span")
        } finally {
            scope.detach()
        }
        assertSameSpanContext(FakeSpanContext.VALID, span.spanContext)
    }

    @Test
    fun `span creation action is not invoked`() {
        var invoked = false
        tracer.startSpan("span", parentContext = contextWithSpan) { invoked = true }
        assertFalse(invoked)
        assertTrue(errorHandler.errors.isEmpty())
    }

    private fun assertSameSpanContext(expected: SpanContext, observed: SpanContext) {
        assertEquals(expected.traceId, observed.traceId)
        assertEquals(expected.spanId, observed.spanId)
        assertEquals(expected.traceFlags.isSampled, observed.traceFlags.isSampled)
    }
}
