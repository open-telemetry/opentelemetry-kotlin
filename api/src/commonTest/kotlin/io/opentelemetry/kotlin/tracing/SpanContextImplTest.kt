package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanContextImplTest {

    private val traceFlags = TraceFlagsImpl(isSampled = false, isRandom = false)
    private val traceState = TraceStateImpl.EMPTY

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
        createInvalidSpanContext().traceIdBytes.fill(1)
        createInvalidSpanContext().spanIdBytes.fill(1)

        val other = createSpanContext(ByteArray(16), ByteArray(8))
        assertFalse(other.isValid)
        assertContentEquals(ByteArray(16), other.traceIdBytes)
        assertContentEquals(ByteArray(8), other.spanIdBytes)
        assertEquals(createInvalidSpanContext(), other)
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
