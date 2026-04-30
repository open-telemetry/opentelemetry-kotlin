package io.opentelemetry.kotlin.tracing.sampling

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class OtelTraceStateTest {
    @Test
    fun parsesOt() {
        val ot = OtelTraceState.parse("rv:123456789abcde;th:123abc")
        assertEquals(0x123456789abcde, ot.rv)
        assertEquals(0x123abc00000000, ot.th)
    }

    @Test
    fun returnsNullForInvalidRv() {
        val nonHex = OtelTraceState.parse("rv:xxxxxxxxxxxxxx")
        val wrongLength = OtelTraceState.parse("rv:123")
        assertNull(nonHex.rv)
        assertNull(wrongLength.rv)
    }

    @Test
    fun returnsNullForInvalidTh() {
        val nonHex = OtelTraceState.parse("th:xxxxxxxxxxxxxx")
        val tooLong = OtelTraceState.parse("th:123456789abcdef")
        val blank = OtelTraceState.parse("th:")
        assertNull(nonHex.th)
        assertNull(tooLong.th)
        assertNull(blank.th)
    }

    @Test
    fun parsesSingleCharThreshold() {
        val ot = OtelTraceState.parse("th:0")
        assertEquals(0L, ot.th)
    }

    @Test
    fun parsesFullLengthThreshold() {
        val ot = OtelTraceState.parse("th:ffffffffffffff")
        assertEquals(0xffffffffffffff, ot.th)
    }

    @Test
    fun skipsEntriesWithoutColon() {
        val ot = OtelTraceState.parse("badentry;rv:123456789abcde")
        assertEquals(0x123456789abcde, ot.rv)
    }

    @Test
    fun keepsFirstValueForDuplicateKeys() {
        val ot = OtelTraceState.parse("rv:11111111111111;rv:22222222222222")
        assertEquals(0x11111111111111, ot.rv)
    }

    @Test
    fun encodesThreshold() {
        val ot = OtelTraceState.parse("")
        ot.setThreshold(0x123abc)
        assertEquals("th:00000000123abc", ot.encode())
    }

    @Test
    fun encodesZeroThreshold() {
        val ot = OtelTraceState.parse("")
        ot.setThreshold(0x0)
        assertEquals("th:0", ot.encode())
    }

    @Test
    fun rejectsNegativeThreshold() {
        val ot = OtelTraceState.parse("")
        assertFailsWith(IllegalArgumentException::class) {
            ot.setThreshold(-1L)
        }
    }

    @Test
    fun rejectsThresholdExceeding14HexDigits() {
        val ot = OtelTraceState.parse("")
        assertFailsWith(IllegalArgumentException::class) {
            ot.setThreshold(0xffffffffffffff + 1)
        }
    }

    @Test
    fun preservesOtherKeys() {
        val ot = OtelTraceState.parse("rv:123456789abcde;th:123")
        ot.setThreshold(0xdef)
        assertEquals(0x123456789abcde, ot.rv)
        assertEquals(0x00000000000def, ot.th)
    }

    @Test
    fun erasesThreshold() {
        val ot = OtelTraceState.parse("rv:123456789abcde;th:123")
        ot.eraseThreshold()
        assertEquals("rv:123456789abcde", ot.encode())
    }
}
