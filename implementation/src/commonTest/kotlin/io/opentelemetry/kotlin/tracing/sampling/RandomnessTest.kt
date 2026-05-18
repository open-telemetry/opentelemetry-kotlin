package io.opentelemetry.kotlin.tracing.sampling

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RandomnessTest {
    @Test
    fun createsRandomnessFromTraceId() {
        val traceIdRandomness = "00000000000123"
        assertEquals(Randomness(0x123), Randomness.fromTraceId("aaaaaaaaaaaaaaaaaa${traceIdRandomness}"))
    }

    @Test
    fun decodesRandomness() {
        assertEquals(Randomness(0x123456789abcde), Randomness.decode("123456789abcde"))
    }

    @Test
    fun decodesInvalidToNull() {
        val nonHex = "xxxxxxxxxxxxxx"
        val tooShort = "123456789abcd"
        val tooLong = "123456789abcdef"
        val blank = ""
        assertNull(Randomness.decode(nonHex))
        assertNull(Randomness.decode(tooShort))
        assertNull(Randomness.decode(tooLong))
        assertNull(Randomness.decode(blank))
    }

    @Test
    fun comparesRandomnessToThreshold() {
        assertTrue(Randomness(1L) < Threshold(2L))
        assertTrue(Randomness(2L) > Threshold(1L))
        assertEquals(Randomness(1L).compareTo(Threshold(1L)), 0)
    }
}