package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.context.NoopContext
import io.opentelemetry.kotlin.context.NoopContextKey
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.tracing.NoopSpan
import io.opentelemetry.kotlin.tracing.NoopSpanContext
import io.opentelemetry.kotlin.tracing.NoopTraceFlags
import io.opentelemetry.kotlin.tracing.model.SpanKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class NoopTests {

    @Test
    fun testNoopTracing() {
        val otel = NoopOpenTelemetry
        val tracerProvider = otel.tracerProvider
        val tracer = tracerProvider.getTracer("test-tracer")

        // All created spans are the same noop instance
        val span = tracer.createSpan("span")
        val anotherSpan = tracer.createSpan("span2", spanKind = SpanKind.CLIENT)
        assertSame(span, anotherSpan)
        assertTrue(span is NoopSpan)

        // Span operations should be no-ops
        verifySpanOperationsAreNoop(span)

        span.end()
        anotherSpan.end(5)

        // Test span context default values
        val context = span.spanContext
        assertEquals("", context.traceId)
        assertEquals("", context.spanId)
        assertFalse(context.isValid)
        assertFalse(context.isRemote)

        // Test trace flags default values
        val traceFlags = context.traceFlags
        assertFalse(traceFlags.isSampled)
        assertFalse(traceFlags.isRandom)

        // Test trace state default values
        val traceState = context.traceState
        assertTrue(traceState.asMap().isEmpty())
        assertEquals(null, traceState.get("any-key"))

        assertSame(traceState, traceState.put("key", "value"))
        assertSame(traceState, traceState.remove("key"))
    }

    @Test
    fun testNoopLogging() {
        val otel = NoopOpenTelemetry
        val loggerProvider = otel.loggerProvider
        val logger = loggerProvider.getLogger("test-logger")

        // Logging does nothing
        logger.emit(
            body = "Complex message",
            timestamp = 1000000L,
            observedTimestamp = 2000000L,
            severityNumber = SeverityNumber.ERROR,
            severityText = "ERROR",
            attributes = {
                setStringAttribute("service", "test-service")
                setBooleanAttribute("success", false)
                setLongAttribute("duration", 1500L)
                setDoubleAttribute("rate", 95.5)

                setStringListAttribute("tags", listOf("test", "noop"))
                setBooleanListAttribute("flags", listOf(true, false))
                setLongListAttribute("numbers", listOf(1L, 2L, 3L))
                setDoubleListAttribute("rates", listOf(1.0, 2.5))
            }
        )
        assertFalse(logger.enabled())
    }

    @Test
    fun testNoopEvent() {
        val otel = NoopOpenTelemetry
        val loggerProvider = otel.loggerProvider
        val logger = loggerProvider.getLogger("test-logger")

        // Logging does nothing
        logger.emit(
            body = "Complex message",
            eventName = "my_event",
            timestamp = 1000000L,
            observedTimestamp = 2000000L,
            context = null,
            severityNumber = SeverityNumber.ERROR,
            severityText = "ERROR",
            attributes = {
                setStringAttribute("service", "test-service")
                setBooleanAttribute("success", false)
                setLongAttribute("duration", 1500L)
                setDoubleAttribute("rate", 95.5)

                setStringListAttribute("tags", listOf("test", "noop"))
                setBooleanListAttribute("flags", listOf(true, false))
                setLongListAttribute("numbers", listOf(1L, 2L, 3L))
                setDoubleListAttribute("rates", listOf(1.0, 2.5))
            }
        )
    }

    @Test
    fun testNoopClockDefault() {
        val otel = NoopOpenTelemetry as OpenTelemetrySdk
        val clock = otel.clock

        // Noop clock always returns 0
        assertEquals(0L, clock.now())
        assertEquals(0L, clock.now())
    }

    @Test
    fun testNoopExplicitContext() {
        val otel = NoopOpenTelemetry
        val ctx = otel.contextFactory.root()

        val key = ctx.createKey<String>("key")
        assertTrue(key is NoopContextKey)

        val other = ctx.set(key, "value")
        assertSame(ctx, other)

        assertNull(ctx.get(key))
    }

    @Test
    fun testNoopImplicitContext() {
        val otel = NoopOpenTelemetry
        val ctx = otel.contextFactory.root()

        // implicit context
        ctx.attach().detach()
        assertNotNull(otel.contextFactory.implicitContext())
    }

    @Test
    fun testNoopSpanContext() {
        val otel = NoopOpenTelemetry
        val invalid = otel.spanContextFactory.invalid
        assertTrue(invalid is NoopSpanContext)
        assertFalse(invalid.isValid)

        val other = otel.spanContextFactory.create(
            otel.tracingIdFactory.generateTraceIdBytes(),
            otel.tracingIdFactory.generateSpanIdBytes(),
            otel.traceFlagsFactory.default,
            otel.traceStateFactory.default
        )
        assertSame(invalid, other)
    }

    @Test
    fun testStoreSpan() {
        val otel = NoopOpenTelemetry
        val span = otel.tracerProvider.getTracer("tracer").createSpan("span")
        val ctx = otel.contextFactory.storeSpan(otel.contextFactory.root(), span)
        assertTrue(ctx is NoopContext)
    }

    @Test
    fun testNoopTraceFlagsFactory() {
        val otel = NoopOpenTelemetry
        val traceFlagsFactory = otel.traceFlagsFactory
        assertTrue(traceFlagsFactory.create(true, random = false) is NoopTraceFlags)
        assertTrue(traceFlagsFactory.fromHex("01") is NoopTraceFlags)
    }

    @Test
    fun testNoopSpan() {
        val otel = NoopOpenTelemetry

        val first = otel.spanFactory.invalid
        assertTrue(first is NoopSpan)
        assertFalse(first.isRecording())

        val second = otel.spanFactory.fromSpanContext(otel.spanContextFactory.invalid)
        assertTrue(second is NoopSpan)

        val third = otel.spanFactory.fromContext(otel.contextFactory.root())
        assertTrue(third is NoopSpan)
    }

    private fun verifySpanOperationsAreNoop(span: NoopSpan) {
        // Test primitive attributes
        span.setStringAttribute("key", "value")
        span.setBooleanAttribute("flag", true)
        span.setLongAttribute("number", 42L)
        span.setDoubleAttribute("decimal", 3.14)

        // Test list attributes
        span.setStringListAttribute("strings", listOf("a", "b", "c"))
        span.setBooleanListAttribute("booleans", listOf(true, false, true))
        span.setLongListAttribute("longs", listOf(1L, 2L, 3L))
        span.setDoubleListAttribute("doubles", listOf(1.1, 2.2, 3.3))

        // Test events
        span.addEvent("test-event")
        span.addEvent("test-event-with-attributes") {
            setStringAttribute("event-key", "event-value")
        }

        // Test links
        span.addLink(span.spanContext) {
            setStringAttribute("link-key", "link-value")
        }

        // Verify no data is recorded
        assertTrue(span.events.isEmpty())
        assertTrue(span.links.isEmpty())
        assertTrue(span.attributes.isEmpty())
        assertFalse(span.isRecording())
        assertFalse(span.parent.isValid)
    }
}
