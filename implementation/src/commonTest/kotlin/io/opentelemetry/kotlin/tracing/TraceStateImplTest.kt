package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TraceStateImplTest {

    @Test
    fun testEmptyTraceStateReturnsNullForKey() {
        val traceState = TraceStateImpl.create()

        assertNull(traceState.get("any-key"))
        assertTrue(traceState.asMap().isEmpty())
    }

    @Test
    fun testTraceStateRetrievesValues() {
        val traceState = TraceStateImpl.create()
            .put("key", "value")
            .put("version", "1.0")

        assertEquals("value", traceState.get("key"))
        assertEquals("1.0", traceState.get("version"))
        assertNull(traceState.get("missing-key"))
    }

    @Test
    fun testAsMapPerformsDeepCopy() {
        val traceState = TraceStateImpl.create()
            .put("key1", "value1")
            .put("key2", "value2")

        val mapFromTraceState = traceState.asMap()
        assertEquals(2, mapFromTraceState.size)
        assertEquals("value1", mapFromTraceState["key1"])
        assertEquals("value2", mapFromTraceState["key2"])

        // Verify it's a copy by adding a pair to the original
        traceState.put("key3", "value3")

        // Original map should be unchanged
        assertEquals(2, mapFromTraceState.size)
        assertNull(mapFromTraceState["key3"])
    }

    @Test
    fun testEmptyValues() {
        val traceState = TraceStateImpl.create()
            .put("empty", "")
            .put("space", " ")

        assertEquals("", traceState.get("empty"))
        assertEquals(" ", traceState.get("space"))
    }

    @Test
    fun testWhitespaceIgnored() {
        val traceState = TraceStateImpl.create()

        // Invalid values with control characters should return same instance
        val result1 = traceState.put("key", "value\twith\ttab")
        assertSame(traceState, result1)

        val result2 = traceState.put("key", "value\nwith\nnewline")
        assertSame(traceState, result2)

        val result3 = traceState.put("key", "value\rwith\rcarriage")
        assertSame(traceState, result3)

        val result4 = traceState.put("key", "value\r\nwith\r\ncrlf")
        assertSame(traceState, result4)
    }

    @Test
    fun testPutAddingNewKvp() {
        val traceState = TraceStateImpl.create()
        val newTraceState = traceState.put("key", "value")

        assertNull(traceState.get("key")) // Original unchanged
        assertEquals("value", newTraceState.get("key"))
    }

    @Test
    fun testPutUpdatesExistingKvp() {
        val original = TraceStateImpl.create().put("vendor", "old-value")
        val updated = original.put("vendor", "new-value")

        assertEquals("old-value", original.get("vendor")) // Original unchanged
        assertEquals("new-value", updated.get("vendor"))
    }

    @Test
    fun testRemovingKeyReturnsNewInstance() {
        val original = TraceStateImpl.create()
            .put("key1", "value1")
            .put("key2", "value2")
        val updated = original.remove("key1")

        assertEquals("value1", original.get("key1")) // Original unchanged
        assertNull(updated.get("key1"))
        assertEquals("value2", updated.get("key2")) // Other keys preserved
    }

    @Test
    fun testRemoveFailureReturnsSameInstance() {
        val original = TraceStateImpl.create().put("key1", "value1")
        val result = original.remove("nonexistent")

        assertSame(original, result) // Same instance
    }

    @Test
    fun testPutValidatesKeyFormat() {
        val traceState = TraceStateImpl.create()

        // Valid simple keys should work
        val result1 = traceState.put("key", "value") // starts with letter
        assertEquals("value", result1.get("key"))

        val result2 = traceState.put("1vendor", "test") // starts with digit
        assertEquals("test", result2.get("1vendor"))

        val result3 = traceState.put("a_b-c*d/e", "test") // allowed characters
        assertEquals("test", result3.get("a_b-c*d/e"))

        // Valid multi-tenant keys should work
        val result4 = traceState.put("tenant@system", "value") // basic multi-tenant
        assertEquals("value", result4.get("tenant@system"))

        val result5 = traceState.put("1tenant@system", "value") // tenant starts with digit
        assertEquals("value", result5.get("1tenant@system"))

        // Invalid keys should return same instance
        val result6 = traceState.put("", "value") // empty key
        assertSame(traceState, result6)

        val result7 = traceState.put(" ", "value") // blank key
        assertSame(traceState, result7)

        val result8 = traceState.put("VENDOR", "value") // uppercase
        assertSame(traceState, result8)

        val result9 = traceState.put("tenant@", "value") // incomplete multi-tenant
        assertSame(traceState, result9)

        val result10 = traceState.put("@system", "value") // incomplete multi-tenant (empty tenant)
        assertSame(traceState, result10)

        val result11 = traceState.put("tenant@System", "value") // system starts with uppercase
        assertSame(traceState, result11)

        val result12 = traceState.put("tenant@system@extra", "value") // multiple @ symbols
        assertSame(traceState, result12)

        val result13 = traceState.put("@", "value") // single @
        assertSame(traceState, result13)
    }

    @Test
    fun testPutValidatesKeyLengthLimit() {
        val traceState = TraceStateImpl.create()

        // Valid simple key at max length (256 chars)
        val maxLengthKey = "a".repeat(256)
        val result1 = traceState.put(maxLengthKey, "value")
        assertEquals("value", result1.get(maxLengthKey))

        // Invalid simple key exceeding max length
        val tooLongKey = "a".repeat(257)
        val result2 = traceState.put(tooLongKey, "value")
        assertSame(traceState, result2)
    }

    @Test
    fun testPutValidatesValueLengthLimit() {
        val traceState = TraceStateImpl.create()

        // Valid value at max length (256 chars)
        val maxLengthValue = "a".repeat(256)
        val result1 = traceState.put("key", maxLengthValue)
        assertEquals(maxLengthValue, result1.get("key"))

        // Invalid value exceeding max length
        val tooLongValue = "a".repeat(257)
        val result2 = traceState.put("key", tooLongValue)
        assertSame(traceState, result2)
    }

    @Test
    fun testPutValidatesMultiTenantLengthLimit() {
        val traceState = TraceStateImpl.create()

        // Valid length limits
        val longTenant = "a".repeat(241) // max tenant length (1 + 0*240)
        val longSystem = "a".repeat(14) // max system length
        val validLongKey = "$longTenant@$longSystem"
        val result1 = traceState.put(validLongKey, "value")
        assertEquals("value", result1.get(validLongKey))

        // Invalid length limits
        val tooLongTenant = "a".repeat(242) // exceeds tenant max
        val invalidKey1 = "$tooLongTenant@system"
        val result2 = traceState.put(invalidKey1, "value")
        assertSame(traceState, result2)

        val tooLongSystem = "a".repeat(15) // exceeds system max
        val invalidKey2 = "tenant@$tooLongSystem"
        val result3 = traceState.put(invalidKey2, "value")
        assertSame(traceState, result3)
    }
}
