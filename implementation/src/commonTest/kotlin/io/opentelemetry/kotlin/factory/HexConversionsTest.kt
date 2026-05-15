package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class HexConversionsTest {

    @Test
    fun hexToByteArrayWorks() {
        val expected = byteArrayOf(0x12, 0x04, 0x80.toByte(), 0xab.toByte(), 0xcd.toByte())
        assertContentEquals(expected, "120480aBcD".hexToByteArray())
    }

    @Test
    fun hexToByteArrayEmptyStringProducesEmptyByteArray() {
        assertContentEquals(ByteArray(0), "".hexToByteArray())
    }

    @Test
    fun hexToByteArrayOddLengthInputReturnsEmptyByteArray() {
        assertContentEquals(ByteArray(0), "abc".hexToByteArray())
    }

    @Test
    fun hexToByteArrayNonHexCharacterReturnsEmptyByteArray() {
        assertContentEquals(ByteArray(0), "0g".hexToByteArray())
    }

    @Test
    fun toHexStringWorks() {
        assertEquals("120480abcd", byteArrayOf(0x12, 0x04, 0x80.toByte(), 0xab.toByte(), 0xcd.toByte()).toHexString())
    }

    @Test
    fun toHexStringEmptyArrayProducesEmptyString() {
        assertEquals("", ByteArray(0).toHexString())
    }

    @Test
    fun toHexStringEmitsLowercaseHex() {
        val hex = byteArrayOf(0xab.toByte(), 0xcd.toByte(), 0xef.toByte()).toHexString()
        assertEquals("abcdef", hex)
        assertEquals(hex, hex.lowercase())
    }

    @Test
    fun toHexStringByteArrayDefaultValueProducesAllZeroString() {
        val bytes = 8
        assertEquals("0".repeat(bytes * 2), ByteArray(bytes).toHexString())
    }
}
