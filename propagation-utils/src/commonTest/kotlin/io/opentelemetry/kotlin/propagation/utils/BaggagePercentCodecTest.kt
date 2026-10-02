package io.opentelemetry.kotlin.propagation.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BaggagePercentCodecTest {

    @Test
    fun testEncodeLeavesBaggageOctetsAlone() {
        assertEquals("abc-123_~", percentEncodeBaggageValue("abc-123_~"))
    }

    @Test
    fun testEncodeEscapesNonOctets() {
        assertEquals("a%20b%2Cc%3Bd%5Ce%22f%25", percentEncodeBaggageValue("a b,c;d\\e\"f%"))
        assertEquals("%C3%A9", percentEncodeBaggageValue("é"))
    }

    @Test
    fun testDecode() {
        assertEquals("plain", percentDecodeBaggageValue("plain"))
        assertEquals("a b", percentDecodeBaggageValue("a%20b"))
        assertEquals("é", percentDecodeBaggageValue("%c3%A9"))
    }

    @Test
    fun testDecodeMalformed() {
        assertNull(percentDecodeBaggageValue("%"))
        assertNull(percentDecodeBaggageValue("abc%2"))
        assertNull(percentDecodeBaggageValue("%zz"))
    }

    @Test
    fun testRoundTrip() {
        val value = "100% ünïcödé, with; delimiters=and\\slashes"
        assertEquals(value, percentDecodeBaggageValue(percentEncodeBaggageValue(value)))
    }
}
