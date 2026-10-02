package io.opentelemetry.kotlin.propagation.utils

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class HexConversionTest {

    @Test
    fun testEncodeHex() {
        assertEquals("", ByteArray(0).encodeHex())
        assertEquals("00017f80ff", byteArrayOf(0, 1, 0x7f, 0x80.toByte(), 0xff.toByte()).encodeHex())
    }

    @Test
    fun testDecodeHex() {
        val expected = byteArrayOf(0, 1, 0x7f, 0x80.toByte(), 0xff.toByte())
        assertContentEquals(expected, "00017f80ff".decodeHexOrEmpty())
        assertContentEquals(expected, "00017F80FF".decodeHexOrEmpty())
        assertContentEquals(ByteArray(0), "".decodeHexOrEmpty())
    }

    @Test
    fun testDecodeInvalidHex() {
        assertContentEquals(ByteArray(0), "abc".decodeHexOrEmpty(), "odd length")
        assertContentEquals(ByteArray(0), "zz".decodeHexOrEmpty(), "non-hex")
        assertContentEquals(ByteArray(0), "٠١".decodeHexOrEmpty(), "non-ascii digits")
    }

    @Test
    fun testRoundTrip() {
        val bytes = ByteArray(TRACE_ID_BYTES) { it.toByte() }
        assertContentEquals(bytes, bytes.encodeHex().decodeHexOrEmpty())
    }

    @Test
    fun testHexDigitValue() {
        assertEquals(0, '0'.hexDigitValue())
        assertEquals(9, '9'.hexDigitValue())
        assertEquals(10, 'a'.hexDigitValue())
        assertEquals(15, 'F'.hexDigitValue())
        assertEquals(-1, 'g'.hexDigitValue())
        assertEquals(-1, '٠'.hexDigitValue())
    }
}
