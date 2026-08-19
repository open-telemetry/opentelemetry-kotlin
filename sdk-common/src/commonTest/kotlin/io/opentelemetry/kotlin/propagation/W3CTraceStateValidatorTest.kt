package io.opentelemetry.kotlin.propagation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Asserts the key/value rules of the W3C `tracestate` header:
 * https://www.w3.org/TR/trace-context/#tracestate-header
 */
internal class W3CTraceStateValidatorTest {

    @Test
    fun testValidSimpleKeys() {
        assertTrue(W3CTraceStateValidator.isValidKey("foo"))
        assertTrue(W3CTraceStateValidator.isValidKey("a"))
        assertTrue(W3CTraceStateValidator.isValidKey("a-b_c*d/e"))
        assertTrue(W3CTraceStateValidator.isValidKey("a0123456789"))
        assertTrue(W3CTraceStateValidator.isValidKey("a".repeat(256)))
    }

    @Test
    fun testInvalidSimpleKeys() {
        assertFalse(W3CTraceStateValidator.isValidKey(""))
        assertFalse(W3CTraceStateValidator.isValidKey("   "))
        assertFalse(W3CTraceStateValidator.isValidKey("UPPERCASE"))
        assertFalse(W3CTraceStateValidator.isValidKey("0leading-digit"))
        assertFalse(W3CTraceStateValidator.isValidKey("has space"))
        assertFalse(W3CTraceStateValidator.isValidKey("has.dot"))
        assertFalse(W3CTraceStateValidator.isValidKey("a".repeat(257)))
    }

    @Test
    fun testValidMultiTenantKeys() {
        assertTrue(W3CTraceStateValidator.isValidKey("tenant@vendor"))
        assertTrue(W3CTraceStateValidator.isValidKey("0tenant@vendor"))
        assertTrue(W3CTraceStateValidator.isValidKey("${"a".repeat(241)}@${"v".repeat(14)}"))
    }

    @Test
    fun testInvalidMultiTenantKeys() {
        assertFalse(W3CTraceStateValidator.isValidKey("@vendor"))
        assertFalse(W3CTraceStateValidator.isValidKey("tenant@"))
        assertFalse(W3CTraceStateValidator.isValidKey("tenant@0vendor"))
        assertFalse(W3CTraceStateValidator.isValidKey("too@many@parts"))
        assertFalse(W3CTraceStateValidator.isValidKey("${"a".repeat(242)}@vendor"))
        assertFalse(W3CTraceStateValidator.isValidKey("tenant@${"v".repeat(15)}"))
    }

    @Test
    fun testValidValues() {
        assertTrue(W3CTraceStateValidator.isValidValue("bar"))
        assertTrue(W3CTraceStateValidator.isValidValue("a b"))
        assertTrue(W3CTraceStateValidator.isValidValue("UPPER-and-symbols!~"))
        assertTrue(W3CTraceStateValidator.isValidValue("v".repeat(256)))
    }

    @Test
    fun testInvalidValues() {
        assertFalse(W3CTraceStateValidator.isValidValue(""))
        assertFalse(W3CTraceStateValidator.isValidValue("has,comma"))
        assertFalse(W3CTraceStateValidator.isValidValue("has=equals"))
        assertFalse(W3CTraceStateValidator.isValidValue("trailing "))
        assertFalse(W3CTraceStateValidator.isValidValue("non-ascii-é"))
        assertFalse(W3CTraceStateValidator.isValidValue("v".repeat(257)))
    }

    @Test
    fun testCanPutRejectsInvalidKeyOrValue() {
        assertFalse(W3CTraceStateValidator.canPut(emptyMap(), "UPPERCASE", "value"))
        assertFalse(W3CTraceStateValidator.canPut(emptyMap(), "key", ""))
        assertTrue(W3CTraceStateValidator.canPut(emptyMap(), "key", "value"))
    }

    @Test
    fun testCanPutRejectsNewKeyBeyondMaxEntries() {
        val entries = (0 until MAX_ENTRIES).associate { "key$it" to "value$it" }
        assertFalse(W3CTraceStateValidator.canPut(entries, "extra", "value"))
    }

    @Test
    fun testCanPutAllowsExistingKeyAtMaxEntries() {
        val entries = (0 until MAX_ENTRIES).associate { "key$it" to "value$it" }
        assertTrue(W3CTraceStateValidator.canPut(entries, "key0", "updated"))
    }

    @Test
    fun testCanPutAllowsNewKeyBelowMaxEntries() {
        val entries = (0 until MAX_ENTRIES - 1).associate { "key$it" to "value$it" }
        assertTrue(W3CTraceStateValidator.canPut(entries, "extra", "value"))
    }

    private companion object {
        private const val MAX_ENTRIES = 32
    }
}
