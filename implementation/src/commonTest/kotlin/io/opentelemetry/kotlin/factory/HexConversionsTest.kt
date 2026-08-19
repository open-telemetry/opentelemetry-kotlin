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
    fun hexToByteArrayNonAsciiCharacterReturnsEmptyByteArray() {
        assertContentEquals(ByteArray(0), "0é".hexToByteArray())
        assertContentEquals(ByteArray(0), "😀".hexToByteArray())
        assertContentEquals(ByteArray(0), "0٠".hexToByteArray())
    }

    @Test
    fun toHexStringByteArrayDefaultValueProducesAllZeroString() {
        val bytes = 8
        assertEquals("0".repeat(bytes * 2), ByteArray(bytes).toHexString())
    }

    @Test
    fun toHexStringPadsBytesBelow0x10() {
        assertEquals("00010f", byteArrayOf(0x00, 0x01, 0x0f).toHexString())
    }

    @Test
    fun toHexStringEncodesByteExtremes() {
        assertEquals("00ff", byteArrayOf(0x00, 0xff.toByte()).toHexString())
    }

    @Test
    fun toHexStringEncodesFullTraceIdWidth() {
        val traceId = ByteArray(16) { (it * 0x11).toByte() }
        assertEquals("00112233445566778899aabbccddeeff", traceId.toHexString())
    }

    @Test
    fun hexRoundTripsForEveryByteValue() {
        val allBytes = ByteArray(256) { it.toByte() }
        assertContentEquals(allBytes, allBytes.toHexString().hexToByteArray())
    }
}
