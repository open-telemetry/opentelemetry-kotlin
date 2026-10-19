package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanContextEntrypointTest {

    private val traceId = "12345678901234567890123456789012"
    private val spanId = "1234567890123456"
    private val invalidTraceId = "00000000000000000000000000000000"
    private val invalidSpanId = "0000000000000000"

    @Test
    fun testInvalidSpanContext() {
        val spanContext = createInvalidSpanContext()
        assertSame(spanContext, createInvalidSpanContext())
        assertEquals(invalidTraceId, spanContext.traceId)
        assertEquals(invalidSpanId, spanContext.spanId)
        assertFalse(spanContext.traceFlags.isSampled)
        assertFalse(spanContext.traceFlags.isRandom)
        assertTrue(spanContext.traceState.asMap().isEmpty())
        assertFalse(spanContext.isValid)
        assertFalse(spanContext.isRemote)
    }

    @Test
    fun testDefaults() {
        val spanContext = createSpanContext(traceId, spanId)
        assertEquals(traceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
        assertEquals(createInvalidSpanContext().traceFlags, spanContext.traceFlags)
        assertSame(createInvalidSpanContext().traceState, spanContext.traceState)
        assertTrue(spanContext.isValid)
        assertFalse(spanContext.isRemote)
    }

    @Test
    fun testAllZeroIdsAreInvalid() {
        assertFalse(createSpanContext(invalidTraceId, spanId).isValid)
        assertFalse(createSpanContext(traceId, invalidSpanId).isValid)
        assertFalse(createSpanContext(ByteArray(16), ByteArray(8)).isValid)
    }

    @Test
    fun testInvalidIdsAreReplacedWithZeros() {
        listOf(
            "123" to "456",
            "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz" to "gggggggggggggggg",
            "invalid" to "bad",
        ).forEach { (traceId, spanId) ->
            val spanContext = createSpanContext(traceId, spanId)
            assertEquals(invalidTraceId, spanContext.traceId)
            assertEquals(invalidSpanId, spanContext.spanId)
            assertFalse(spanContext.isValid)
        }
    }

    @Test
    fun testInvalidTraceIdDoesNotAffectSpanId() {
        val spanContext = createSpanContext("bad", spanId)
        assertEquals(invalidTraceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
    }

    @Test
    fun testWrongLengthBytesAreReplacedWithZeros() {
        val spanContext = createSpanContext(ByteArray(4) { 1 }, ByteArray(0))
        assertEquals(invalidTraceId, spanContext.traceId)
        assertEquals(invalidSpanId, spanContext.spanId)
        assertEquals(16, spanContext.traceIdBytes.size)
        assertEquals(8, spanContext.spanIdBytes.size)
        assertFalse(spanContext.isValid)
    }

    @Test
    fun testUppercaseHexIsAccepted() {
        val traceId = "ABCDEF1234567890ABCDEF1234567890"
        val spanId = "ABCDEF1234567890"
        val spanContext = createSpanContext(traceId, spanId)
        assertEquals(traceId.lowercase(), spanContext.traceId)
        assertEquals(spanId.lowercase(), spanContext.spanId)
        assertTrue(spanContext.isValid)
    }

    @Test
    fun testBytesOverload() {
        val traceIdBytes = ByteArray(16) { 1 }
        val spanIdBytes = ByteArray(8) { 2 }
        val spanContext = createSpanContext(traceIdBytes, spanIdBytes) {
            isRemote = true
        }
        assertEquals("01".repeat(16), spanContext.traceId)
        assertEquals("02".repeat(8), spanContext.spanId)
        assertTrue(spanContext.isValid)
        assertTrue(spanContext.isRemote)
        assertEquals(createSpanContext("01".repeat(16), "02".repeat(8)) { isRemote = true }, spanContext)
    }

    @Test
    fun testTraceFlags() {
        listOf(
            Pair(false, false),
            Pair(true, false),
            Pair(false, true),
            Pair(true, true),
        ).forEach { (sampled, random) ->
            val flags = createSpanContext(traceId, spanId) {
                isSampled = sampled
                isRandom = random
            }.traceFlags
            assertEquals(sampled, flags.isSampled)
            assertEquals(random, flags.isRandom)
            assertSame(
                flags,
                createSpanContext(traceId, spanId) {
                    isSampled = sampled
                    isRandom = random
                }.traceFlags
            )
        }
    }

    @Test
    fun testPropertiesPreservedWithInvalidIds() {
        val spanContext = createSpanContext("invalid", "bad") {
            isSampled = true
            traceState { put("key", "value") }
        }
        assertFalse(spanContext.isValid)
        assertTrue(spanContext.traceFlags.isSampled)
        assertEquals(mapOf("key" to "value"), spanContext.traceState.asMap())
    }

    @Test
    fun testTraceStatePreservesInsertionOrder() {
        val traceState = createSpanContext(traceId, spanId) {
            traceState {
                put("key1", "value1")
                put("key2", "value2")
                put("key3", "value3")
            }
        }.traceState
        assertEquals(listOf("key1", "key2", "key3"), traceState.asMap().keys.toList())
    }

    @Test
    fun testTraceStateDuplicateKeyReplacesValueInPlace() {
        val traceState = createSpanContext(traceId, spanId) {
            traceState {
                put("key1", "value1")
                put("key2", "value2")
                put("key1", "updated")
            }
        }.traceState
        assertEquals(listOf("key1" to "updated", "key2" to "value2"), traceState.asMap().toList())
    }

    @Test
    fun testTraceStateDropsInvalidEntries() {
        val traceState = createSpanContext(traceId, spanId) {
            traceState {
                put("VENDOR", "value")
                put("key", "value ")
                put("good", "value")
            }
        }.traceState
        assertEquals(mapOf("good" to "value"), traceState.asMap())
    }

    @Test
    fun testTraceStateDropsEntriesOverLimit() {
        val traceState = createSpanContext(traceId, spanId) {
            traceState {
                for (i in 1..33) {
                    put("key$i", "value$i")
                }
            }
        }.traceState
        assertEquals(32, traceState.asMap().size)
        assertNull(traceState.get("key33"))
    }

    @Test
    fun testTraceStateLastCallWins() {
        val traceState = createSpanContext(traceId, spanId) {
            traceState { put("key1", "value1") }
            traceState { put("key2", "value2") }
        }.traceState
        assertEquals(mapOf("key2" to "value2"), traceState.asMap())
    }

    @Test
    fun testEmptyTraceStateIsShared() {
        val spanContext = createSpanContext(traceId, spanId) {
            traceState { }
        }
        assertSame(createInvalidSpanContext().traceState, spanContext.traceState)
    }

    @Test
    fun testThrowingActionReturnsInvalidSpanContext() {
        val spanContext = createSpanContext(traceId, spanId) {
            isSampled = true
            error("boom")
        }
        assertSame(createInvalidSpanContext(), spanContext)
    }

    @Test
    fun testThrowingTraceStateActionReturnsInvalidSpanContext() {
        val spanContext = createSpanContext(traceId, spanId) {
            traceState { error("boom") }
        }
        assertSame(createInvalidSpanContext(), spanContext)
    }
}
