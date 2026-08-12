package io.opentelemetry.kotlin.tracing.sampling

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SamplingRandomnessTest {

    @Test
    fun testRandomnessUsesLeastSignificant7Bytes() {
        // chars 18..31 of the trace ID supply the 56-bit randomness value
        assertEquals(0x23456789abcdefL, randomnessFromTraceId("0123456789abcdef0123456789abcdef"))
    }

    @Test
    fun testRandomnessMaxValue() {
        assertEquals(0x00FFFFFFFFFFFFFFL, randomnessFromTraceId("ffffffffffffffffffffffffffffffff"))
    }

    @Test
    fun testRandomnessZeroValue() {
        assertEquals(0L, randomnessFromTraceId("00000000000000000000000000000000"))
    }

    @Test
    fun testRandomnessAcceptsUppercaseHex() {
        assertEquals(
            randomnessFromTraceId("0123456789abcdef0123456789abcdef"),
            randomnessFromTraceId("0123456789ABCDEF0123456789ABCDEF"),
        )
    }

    @Test
    fun testRandomnessZeroForTooShortTraceId() {
        assertEquals(0L, randomnessFromTraceId("0123456789abcdef"))
    }

    @Test
    fun testRandomnessZeroForEmptyTraceId() {
        assertEquals(0L, randomnessFromTraceId(""))
    }

    @Test
    fun testRandomnessZeroForTooLongTraceId() {
        assertEquals(0L, randomnessFromTraceId("0123456789abcdef0123456789abcdef0"))
    }

    @Test
    fun testRandomnessZeroForNonHexCharInRandomnessWindow() {
        assertEquals(0L, randomnessFromTraceId("0123456789abcdef0123456789abcdeg"))
    }

    @Test
    fun testRandomnessZeroForNonHexCharOutsideRandomnessWindow() {
        assertEquals(0L, randomnessFromTraceId("g123456789abcdef0123456789abcdef"))
    }
}
