package io.opentelemetry.kotlin.propagation.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BaggageValidationTest {

    @Test
    fun testValidKeys() {
        assertTrue(isValidBaggageKey("key"))
        assertTrue(isValidBaggageKey("Key-1_2.3"))
        assertTrue(isValidBaggageKey("!#$%&'*+-.^_`|~"))
    }

    @Test
    fun testInvalidKeys() {
        assertFalse(isValidBaggageKey(""))
        assertFalse(isValidBaggageKey("a b"))
        assertFalse(isValidBaggageKey("a=b"))
        assertFalse(isValidBaggageKey("a,b"))
        assertFalse(isValidBaggageKey("a;b"))
        assertFalse(isValidBaggageKey("ключ"))
    }

    @Test
    fun testValues() {
        assertTrue(isValidBaggageValue(""))
        assertTrue(isValidBaggageValue("value with spaces, commas; and ünïcödé"))
        assertFalse(isValidBaggageValue("a\rb"))
        assertFalse(isValidBaggageValue("a\nb"))
        assertFalse(isValidBaggageValue("a\u0000b"))
    }
}
