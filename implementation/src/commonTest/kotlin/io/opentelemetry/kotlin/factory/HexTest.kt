package io.opentelemetry.kotlin.factory

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HexTest {

    @Test
    fun testDigitIsHexDigit() {
        assertTrue('0'.isHexDigit())
        assertTrue('9'.isHexDigit())
    }

    @Test
    fun testLowercaseHexLetterIsHexDigit() {
        assertTrue('a'.isHexDigit())
        assertTrue('f'.isHexDigit())
    }

    @Test
    fun testUppercaseHexLetterIsHexDigit() {
        assertTrue('A'.isHexDigit())
        assertTrue('F'.isHexDigit())
    }

    @Test
    fun testNonHexLetterIsNotHexDigit() {
        assertFalse('g'.isHexDigit())
        assertFalse('G'.isHexDigit())
    }

    @Test
    fun testSpaceIsNotHexDigit() {
        assertFalse(' '.isHexDigit())
    }

    @Test
    fun testHyphenIsNotHexDigit() {
        assertFalse('-'.isHexDigit())
    }

    @Test
    fun testEmptyStringIsValidHex() {
        assertTrue("".isValidHex())
    }

    @Test
    fun testAllDigitsIsValidHex() {
        assertTrue("0123456789".isValidHex())
    }

    @Test
    fun testLowercaseLettersIsValidHex() {
        assertTrue("abcdef".isValidHex())
    }

    @Test
    fun testUppercaseLettersIsValidHex() {
        assertTrue("ABCDEF".isValidHex())
    }

    @Test
    fun testMixedCaseIsValidHex() {
        assertTrue("aAbBcCdDeEfF0123456789".isValidHex())
    }

    @Test
    fun testTraceIdIsValidHex() {
        assertTrue("2cc2b48c50aefe53b3974ed91e6b4ea9".isValidHex())
    }

    @Test
    fun testStringWithGIsNotValidHex() {
        assertFalse("abcdefg".isValidHex())
    }

    @Test
    fun testStringWithSpaceIsNotValidHex() {
        assertFalse("abc def".isValidHex())
    }

    @Test
    fun testStringWithHyphenIsNotValidHex() {
        assertFalse("abc-def".isValidHex())
    }

    @Test
    fun testMixedValidAndInvalidIsNotValidHex() {
        assertFalse("0123456789abcdefg".isValidHex())
    }
}
