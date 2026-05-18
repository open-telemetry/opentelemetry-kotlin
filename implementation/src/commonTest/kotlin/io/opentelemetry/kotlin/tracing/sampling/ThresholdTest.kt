package io.opentelemetry.kotlin.tracing.sampling

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class ThresholdTest {
    @Test
    fun decodesInvalidToNull() {
        val nonHex = "xxxxxxxxxxxxxx"
        val tooLong = "123456789abcdef"
        val blank = ""
        assertNull(Threshold.decode(nonHex))
        assertNull(Threshold.decode(tooLong))
        assertNull(Threshold.decode(blank))
    }

    @Test
    fun decodesSingleCharThreshold() {
        assertEquals(Threshold(0L), Threshold.decode("0"))
    }

    @Test
    fun decodesFullLengthThreshold() {
        assertEquals(Threshold(0xffffffffffffff), Threshold.decode("ffffffffffffff"))
    }

    @Test
    fun encodesThreshold() {
        val threshold = Threshold(0x123abc)
        assertEquals("00000000123abc", threshold.encode())
    }

    @Test
    fun encodesZeroThreshold() {
        val threshold = Threshold(0x0)
        assertEquals("0", threshold.encode())
    }

    @Test
    fun rejectsNegativeThreshold() {
        assertFailsWith(IllegalArgumentException::class) {
            Threshold(-1L)
        }
    }

    @Test
    fun rejectsThresholdExceeding14HexDigits() {
        assertFailsWith(IllegalArgumentException::class) {
            Threshold(0xffffffffffffff + 1)
        }
    }
}