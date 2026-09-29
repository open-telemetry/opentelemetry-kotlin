package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OtelJavaSpanContextExtTest {

    @Test
    fun toOtelKotlinSpanContext() {
        val impl = OtelJavaSpanContext.create(
            "12345678901234567890123456789012",
            "1234567890123456",
            OtelJavaTraceFlags.getDefault(),
            OtelJavaTraceState.getDefault()
        )
        val spanContext = impl.toOtelKotlinSpanContext()
        assertEquals(impl.spanId, spanContext.spanId)
        assertEquals(impl.traceId, spanContext.traceId)
        assertTrue(spanContext.isValid)
        assertFalse(spanContext.isRemote)
    }

    @Test
    fun `remote span context copies flags and state`() {
        val impl = OtelJavaSpanContext.createFromRemoteParent(
            "12345678901234567890123456789012",
            "1234567890123456",
            OtelJavaTraceFlags.getSampled(),
            OtelJavaTraceState.builder().put("vendor", "value").build()
        )
        val spanContext = impl.toOtelKotlinSpanContext()
        assertTrue(spanContext.isRemote)
        assertTrue(spanContext.traceFlags.isSampled)
        assertEquals(mapOf("vendor" to "value"), spanContext.traceState.asMap())
    }

    @Test
    fun `random flag is copied and unknown flag bits are ignored`() {
        val random = OtelJavaSpanContext.create(
            "12345678901234567890123456789012",
            "1234567890123456",
            OtelJavaTraceFlags.fromByte(0b01100010),
            OtelJavaTraceState.getDefault()
        ).toOtelKotlinSpanContext()
        assertTrue(random.traceFlags.isRandom)
        assertFalse(random.traceFlags.isSampled)
    }

    @Test
    fun `invalid java span context converts to invalid kotlin span context`() {
        val spanContext = OtelJavaSpanContext.getInvalid().toOtelKotlinSpanContext()
        assertFalse(spanContext.isValid)
        assertEquals("00000000000000000000000000000000", spanContext.traceId)
        assertEquals("0000000000000000", spanContext.spanId)
    }
}
