package io.opentelemetry.kotlin.propagation.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TracingIdValidationTest {

    @Test
    fun testTraceIdBytes() {
        assertTrue(ByteArray(TRACE_ID_BYTES) { 1 }.isValidTraceIdBytes())
        assertFalse(ByteArray(TRACE_ID_BYTES).isValidTraceIdBytes(), "all zeros")
        assertFalse(ByteArray(SPAN_ID_BYTES) { 1 }.isValidTraceIdBytes(), "wrong size")
        assertFalse(ByteArray(0).isValidTraceIdBytes(), "empty")
    }

    @Test
    fun testSpanIdBytes() {
        assertTrue(ByteArray(SPAN_ID_BYTES) { 1 }.isValidSpanIdBytes())
        assertFalse(ByteArray(SPAN_ID_BYTES).isValidSpanIdBytes(), "all zeros")
        assertFalse(ByteArray(TRACE_ID_BYTES) { 1 }.isValidSpanIdBytes(), "wrong size")
        assertFalse(ByteArray(0).isValidSpanIdBytes(), "empty")
    }

    @Test
    fun testAllZeroBytes() {
        assertTrue(ByteArray(0).isAllZeroBytes())
        assertTrue(ByteArray(4).isAllZeroBytes())
        assertFalse(byteArrayOf(0, 0, 1).isAllZeroBytes())
    }

    @Test
    fun testDecodeTraceFlags() {
        assertEquals(0, "00".decodeTraceFlagsOrZero())
        assertEquals(TRACE_FLAG_SAMPLED, "01".decodeTraceFlagsOrZero())
        assertEquals(TRACE_FLAG_RANDOM, "02".decodeTraceFlagsOrZero())
        assertEquals(TRACE_FLAG_SAMPLED or TRACE_FLAG_RANDOM, "03".decodeTraceFlagsOrZero())
        assertEquals(TRACE_FLAG_SAMPLED or TRACE_FLAG_RANDOM, "FF".decodeTraceFlagsOrZero(), "unknown bits")
        assertEquals(0, "0".decodeTraceFlagsOrZero(), "too short")
        assertEquals(0, "001".decodeTraceFlagsOrZero(), "too long")
        assertEquals(0, "zz".decodeTraceFlagsOrZero(), "not hex")
    }

    @Test
    fun testEncodeTraceFlags() {
        assertEquals("00", encodeTraceFlags(isSampled = false, isRandom = false))
        assertEquals("01", encodeTraceFlags(isSampled = true, isRandom = false))
        assertEquals("02", encodeTraceFlags(isSampled = false, isRandom = true))
        assertEquals("03", encodeTraceFlags(isSampled = true, isRandom = true))
    }
}
