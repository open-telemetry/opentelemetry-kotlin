package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SpanContextImplTest {

    private val traceFlags = TraceFlagsFactoryImpl().default
    private val traceState = TraceStateFactoryImpl().default

    @Test
    fun testValidIds() {
        val spanContext = SpanContextImpl(
            traceIdBytes = ByteArray(16) { 1 },
            spanIdBytes = ByteArray(8) { 1 },
            traceFlags = traceFlags,
            isRemote = false,
            traceState = traceState,
        )
        assertTrue(spanContext.isValid)
    }

    @Test
    fun testWrongLengthTraceIdIsInvalid() {
        val spanContext = SpanContextImpl(
            traceIdBytes = ByteArray(4) { 1 },
            spanIdBytes = ByteArray(8) { 1 },
            traceFlags = traceFlags,
            isRemote = false,
            traceState = traceState,
        )
        assertFalse(spanContext.isValid)
    }

    @Test
    fun testWrongLengthSpanIdIsInvalid() {
        val spanContext = SpanContextImpl(
            traceIdBytes = ByteArray(16) { 1 },
            spanIdBytes = ByteArray(3) { 1 },
            traceFlags = traceFlags,
            isRemote = false,
            traceState = traceState,
        )
        assertFalse(spanContext.isValid)
    }
}
