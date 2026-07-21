package io.opentelemetry.kotlin.tracing.sampling

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OtelTraceStateTest {
    @Test
    fun parsesOt() {
        val ot = OtelTraceState.parse("rv:123456789abcde;th:123abc")
        assertEquals(Randomness(0x123456789abcde), ot.rv)
        assertEquals(Threshold(0x123abc00000000), ot.th)
    }

    @Test
    fun returnsNullForInvalidRv() {
        val nonHex = OtelTraceState.parse("rv:xxxxxxxxxxxxxx")
        val wrongLength = OtelTraceState.parse("rv:123")
        assertNull(nonHex.rv)
        assertNull(wrongLength.rv)
    }

    @Test
    fun skipsEntriesWithoutColon() {
        val ot = OtelTraceState.parse("badentry;rv:123456789abcde")
        assertEquals(Randomness(0x123456789abcde), ot.rv)
    }

    @Test
    fun keepsFirstValueForDuplicateKeys() {
        val ot = OtelTraceState.parse("rv:11111111111111;rv:22222222222222")
        assertEquals(Randomness(0x11111111111111), ot.rv)
    }

    @Test
    fun preservesOtherKeys() {
        val ot = OtelTraceState.parse("rv:123456789abcde;th:00000000000123")
        ot.applyThreshold(Threshold(0xdef))
        assertEquals(Randomness(0x123456789abcde), ot.rv)
        assertEquals(Threshold(0x00000000000def), ot.th)
    }

    @Test
    fun erasesThreshold() {
        val ot = OtelTraceState.parse("rv:123456789abcde;th:123")
        ot.eraseThreshold()
        assertEquals("rv:123456789abcde", ot.encode())
    }
}
