package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class TraceStateFactoryImplTest {

    private val factory = TraceStateFactoryImpl()

    @Test
    fun testDefaultTraceState() {
        val traceState = factory.default
        assertNull(traceState.get("any-key"))
        assertTrue(traceState.asMap().isEmpty())
    }

    @Test
    fun testBuildWithNoEntriesReturnsSharedEmpty() {
        assertSame(createInvalidSpanContext().traceState, buildTraceState {})
    }

    @Test
    fun testBuildPreservesInsertionOrder() {
        val traceState = buildTraceState {
            put("key1", "value1")
            put("key2", "value2")
            put("key3", "value3")
        }
        assertEquals(listOf("key1", "key2", "key3"), traceState.asMap().keys.toList())
    }

    @Test
    fun testBuildDuplicateKeyReplacesValueInPlace() {
        val traceState = buildTraceState {
            put("key1", "value1")
            put("key2", "value2")
            put("key1", "updated")
        }
        assertEquals(listOf("key1" to "updated", "key2" to "value2"), traceState.asMap().toList())
    }

    @Test
    fun testBuildDropsInvalidEntries() {
        val traceState = buildTraceState {
            put("VENDOR", "value")
            put("key", "value ")
            put("good", "value")
        }
        assertEquals(mapOf("good" to "value"), traceState.asMap())
    }

    @Test
    fun testBuildDropsEntriesOverLimit() {
        val traceState = buildTraceState {
            for (i in 1..33) {
                put("key$i", "value$i")
            }
        }
        assertEquals(32, traceState.asMap().size)
        assertNull(traceState.get("key33"))
    }
}
