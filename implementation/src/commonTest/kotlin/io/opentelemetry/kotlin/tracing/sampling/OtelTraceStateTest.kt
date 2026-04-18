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
        assertEquals("rv:123456789abcde;th:00000000000def", ot.encode())
    }
}
