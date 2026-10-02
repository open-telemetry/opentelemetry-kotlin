package io.opentelemetry.kotlin.propagation.utils

import kotlin.test.Test
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
}
