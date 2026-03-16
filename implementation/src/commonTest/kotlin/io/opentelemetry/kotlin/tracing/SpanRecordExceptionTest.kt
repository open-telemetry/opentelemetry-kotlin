package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.factory.FakeTraceStateFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.semconv.ExceptionAttributes
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanRecordExceptionTest {

    private val scope = InstrumentationScopeInfoImpl("test", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var processor: FakeSpanProcessor

    @BeforeTest
    fun setUp() {
        processor = FakeSpanProcessor()
        tracer = TracerImpl(
            clock = FakeClock(),
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            traceStateFactory = FakeTraceStateFactory(),
            spanFactory = FakeSpanFactory(),
            scope = scope,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = FakeIdGenerator(),
            shutdownState = MutableShutdownState(),
        )
    }

    @Test
    fun testRecordException() {
        tracer.startSpan("test").apply {
            recordException(IllegalStateException("something went wrong"))
            end()
        }

        val attrs = processor.endCalls.single().events.single().also {
            assertEquals("exception", it.name)
        }.attributes
        assertNotNull(attrs[ExceptionAttributes.EXCEPTION_STACKTRACE])
        assertEquals("something went wrong", attrs[ExceptionAttributes.EXCEPTION_MESSAGE])
        assertTrue((attrs[ExceptionAttributes.EXCEPTION_TYPE] as String).contains("IllegalStateException"))
    }

    @Test
    fun testRecordExceptionMinimal() {
        tracer.startSpan("test").apply {
            recordException(object : RuntimeException() {})
            end()
        }

        val attrs = processor.endCalls.single().events.single().attributes
        assertNotNull(attrs[ExceptionAttributes.EXCEPTION_STACKTRACE])
        assertNull(attrs[ExceptionAttributes.EXCEPTION_MESSAGE])
        assertNull(attrs[ExceptionAttributes.EXCEPTION_TYPE])
    }

    @Test
    fun testRecordExceptionMergeAttrs() {
        tracer.startSpan("test").apply {
            recordException(RuntimeException("oops")) {
                setStringAttribute("custom.key", "custom.value")
            }
            end()
        }

        val attrs = processor.endCalls.single().events.single().attributes
        assertNotNull(attrs[ExceptionAttributes.EXCEPTION_STACKTRACE])
        assertEquals("custom.value", attrs["custom.key"])
    }
}
