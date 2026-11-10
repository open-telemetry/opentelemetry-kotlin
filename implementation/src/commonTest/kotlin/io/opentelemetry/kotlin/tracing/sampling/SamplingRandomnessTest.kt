package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.factory.hexToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SamplingRandomnessTest {

    @Test
    fun testDefaultThresholdPrecisionMatchesSpecificationExamples() {
        val thresholds = mapOf(
            1.0 to "0",
            0.5 to "8",
            1.0 / 3 to "aaab",
            0.25 to "c",
            0.2 to "cccd",
            0.125 to "e",
            0.1 to "e666",
            0.0625 to "f",
            0.01 to "fd70a",
            0.001 to "ffbe77",
            0.0001 to "fff9724",
            0.00001 to "ffff583a",
            0.000001 to "ffffef39",
        )
        for ((ratio, expected) in thresholds) {
            val traceState = OtelTraceState.parse(null)
            traceState.setThreshold(thresholdFromRatio(ratio))
            assertEquals("th:$expected", traceState.encode(), "ratio=$ratio")
        }
    }

    @Test
    fun testThresholdKeepsMinimumSamplingProbabilities() {
        val maxThreshold = 1L shl 56
        assertEquals(maxThreshold - 1, thresholdFromRatio(MIN_SAMPLING_RATIO))
        assertEquals(maxThreshold - 2, thresholdFromRatio(2 * MIN_SAMPLING_RATIO))
    }

    @Test
    fun testThresholdSupportsDifferentPrecisions() {
        assertEquals(0xe6600000000000L, thresholdFromRatio(0.1, precision = 3))
        assertEquals(0xe6666000000000L, thresholdFromRatio(0.1, precision = 5))
        assertEquals(0xfd710000000000L, thresholdFromRatio(0.01, precision = 3))
        assertEquals(0xfd70a400000000L, thresholdFromRatio(0.01, precision = 5))
        assertEquals(0xffffffffffffffL, thresholdFromRatio(MIN_SAMPLING_RATIO, precision = 1))
    }

    @Test
    fun testThresholdRejectsInvalidPrecision() {
        for (precision in listOf(0, 15)) {
            assertFailsWith<IllegalArgumentException> {
                thresholdFromRatio(0.1, precision)
            }
        }
    }

    @Test
    fun testThresholdQuantizesOriginalProbabilityAroundMidpoints() {
        val maxThreshold = 1L shl 56
        for (precision in 1..12) {
            val midpoint = (1L shl (4 * precision)) + 8
            for ((offset, sampledCount) in mapOf(-0.25 to midpoint - 8, 0.0 to midpoint - 8, 0.25 to midpoint + 8)) {
                val ratio = (midpoint + offset) / maxThreshold
                assertEquals(
                    maxThreshold - sampledCount,
                    thresholdFromRatio(ratio, precision),
                    "precision=$precision, offset=$offset",
                )
            }
        }
    }

    @Test
    fun testFullPrecisionRoundsOnceAndPreservesWholeCounts() {
        val maxThreshold = 1L shl 56
        for ((sampledCount, expectedCount) in mapOf(1.25 to 1L, 1.5 to 1L, 1.75 to 2L)) {
            assertEquals(
                maxThreshold - expectedCount,
                thresholdFromRatio(sampledCount / maxThreshold, precision = 14),
            )
        }
        val wholeCount = (1L shl 52) + 1
        assertEquals(maxThreshold - wholeCount, thresholdFromRatio(wholeCount.toDouble() / maxThreshold, precision = 14))
    }

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
