package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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

    @Test
    fun testEqualWhenFieldsMatch() {
        val a = create()
        val b = create()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun testNotEqualWhenAnyFieldDiffers() {
        val base = create()
        assertNotEquals(base, create(traceIdBytes = ByteArray(16) { 2 }))
        assertNotEquals(base, create(spanIdBytes = ByteArray(8) { 2 }))
        assertNotEquals(base, create(traceFlags = TraceFlagsImpl(isSampled = true, isRandom = false)))
        assertNotEquals(base, create(isRemote = true))
        assertNotEquals(base, create(traceState = TraceStateImpl.EMPTY.put("key", "value")))
    }

    @Test
    fun testInputArraysAreCopied() {
        val traceIdBytes = ByteArray(16) { 1 }
        val spanIdBytes = ByteArray(8) { 1 }
        val spanContext = create(traceIdBytes = traceIdBytes, spanIdBytes = spanIdBytes)
        traceIdBytes.fill(0)
        spanIdBytes.fill(0)

        assertEquals(create(), spanContext)
        assertContentEquals(ByteArray(16) { 1 }, spanContext.traceIdBytes)
        assertContentEquals(ByteArray(8) { 1 }, spanContext.spanIdBytes)
    }

    @Test
    fun testReturnedArraysAreCopied() {
        val spanContext = create()
        val traceId = spanContext.traceId
        val spanId = spanContext.spanId
        spanContext.traceIdBytes.fill(0)
        spanContext.spanIdBytes.fill(0)

        assertTrue(spanContext.isValid)
        assertEquals(traceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
        assertContentEquals(ByteArray(16) { 1 }, spanContext.traceIdBytes)
        assertContentEquals(ByteArray(8) { 1 }, spanContext.spanIdBytes)
    }

    @Test
    fun testInvalidContextCannotBeCorrupted() {
        val factory = SpanContextFactoryImpl(TraceFlagsFactoryImpl(), TraceStateFactoryImpl())
        factory.invalid.traceIdBytes.fill(1)
        factory.invalid.spanIdBytes.fill(1)

        val other = factory.create(ByteArray(16), ByteArray(8), traceFlags, traceState, false)
        assertFalse(other.isValid)
        assertContentEquals(ByteArray(16), other.traceIdBytes)
        assertContentEquals(ByteArray(8), other.spanIdBytes)
        assertEquals(factory.invalid, other)
    }

    private fun create(
        traceIdBytes: ByteArray = ByteArray(16) { 1 },
        spanIdBytes: ByteArray = ByteArray(8) { 1 },
        traceFlags: TraceFlags = this.traceFlags,
        isRemote: Boolean = false,
        traceState: TraceState = this.traceState,
    ) = SpanContextImpl(
        traceIdBytes = traceIdBytes,
        spanIdBytes = spanIdBytes,
        traceFlags = traceFlags,
        isRemote = isRemote,
        traceState = traceState,
    )
}
