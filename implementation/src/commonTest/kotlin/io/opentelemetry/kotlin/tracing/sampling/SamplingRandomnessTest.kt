package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.factory.hexToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SamplingRandomnessTest {

    @Test
    fun testRandomnessUsesLeastSignificant7Bytes() {
        // bytes 9..15 of the trace ID supply the 56-bit randomness value
        assertEquals(
            0x23456789abcdefL,
            randomnessFromTraceIdBytes("0123456789abcdef0123456789abcdef".hexToByteArray()),
        )
    }

    @Test
    fun testRandomnessMaxValue() {
        assertEquals(
            0x00FFFFFFFFFFFFFFL,
            randomnessFromTraceIdBytes("ffffffffffffffffffffffffffffffff".hexToByteArray()),
        )
    }

    @Test
    fun testRandomnessZeroValue() {
        assertEquals(0L, randomnessFromTraceIdBytes(ByteArray(16)))
    }

    @Test
    fun testRandomnessIgnoresMostSignificant9Bytes() {
        val leadingZeros = ByteArray(16).also { it.fill(0x11, fromIndex = 9) }
        val leadingOnes = ByteArray(16) { 0xFF.toByte() }.also { it.fill(0x11, fromIndex = 9) }
        assertEquals(0x11111111111111L, randomnessFromTraceIdBytes(leadingZeros))
        assertEquals(0x11111111111111L, randomnessFromTraceIdBytes(leadingOnes))
    }

    @Test
    fun testRandomnessZeroForTooShortTraceId() {
        assertEquals(0L, randomnessFromTraceIdBytes(ByteArray(8)))
    }

    @Test
    fun testRandomnessZeroForEmptyTraceId() {
        assertEquals(0L, randomnessFromTraceIdBytes(ByteArray(0)))
    }

    @Test
    fun testRandomnessZeroForTooLongTraceId() {
        assertEquals(0L, randomnessFromTraceIdBytes(ByteArray(17)))
    }
}
