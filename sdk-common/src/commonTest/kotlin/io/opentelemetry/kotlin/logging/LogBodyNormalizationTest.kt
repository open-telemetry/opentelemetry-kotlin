package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.attributes.AnyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class LogBodyNormalizationTest {

    @Test
    fun testScalarsArePreserved() {
        assertNull(normalizeLogBody(null))
        assertSame(AnyValue.NullValue, normalizeLogBody(AnyValue.NullValue))
        assertEquals("s", normalizeLogBody("s"))
        assertEquals(true, normalizeLogBody(true))
        assertEquals(5L, normalizeLogBody(5L))
        assertEquals(1.5, normalizeLogBody(1.5))
    }

    @Test
    fun testFloatIsWidened() {
        assertEquals(1.5, normalizeLogBody(1.5f))
    }

    @Test
    fun testByteArrayIsCopied() {
        val bytes = byteArrayOf(1, 2)
        val observed = normalizeLogBody(bytes)
        bytes[0] = 9
        assertEquals(AnyValue.BytesValue(byteArrayOf(1, 2)), observed)
    }

    @Test
    fun testCollectionsAndMapsAreConverted() {
        val body = mapOf(
            "list" to listOf("a", 1L, null),
            "array" to arrayOf<Any>(true, 2.5f),
            "set" to setOf(3L),
            "nested" to mapOf(1L to "one"),
        )
        val expected = AnyValue.MapValue(
            mapOf(
                "list" to AnyValue.ListValue(
                    listOf(AnyValue.StringValue("a"), AnyValue.LongValue(1), AnyValue.NullValue)
                ),
                "array" to AnyValue.ListValue(listOf(AnyValue.BoolValue(true), AnyValue.DoubleValue(2.5))),
                "set" to AnyValue.ListValue(listOf(AnyValue.LongValue(3))),
                "nested" to AnyValue.MapValue(mapOf("1" to AnyValue.StringValue("one"))),
            )
        )
        assertEquals(expected, normalizeLogBody(body))
    }

    @Test
    fun testMutableCollectionsAreCopied() {
        val list = mutableListOf<Any>("a")
        val observed = normalizeLogBody(list)
        list.add("b")
        assertEquals(AnyValue.ListValue(listOf(AnyValue.StringValue("a"))), observed)
    }

    @Test
    fun testAnyValueIsDeepCopied() {
        val bytes = byteArrayOf(1)
        val values = mutableListOf<AnyValue>(AnyValue.BytesValue(bytes))
        val observed = normalizeLogBody(AnyValue.MapValue(mapOf("k" to AnyValue.ListValue(values))))
        bytes[0] = 9
        values.add(AnyValue.NullValue)

        val expected = AnyValue.MapValue(
            mapOf("k" to AnyValue.ListValue(listOf(AnyValue.BytesValue(byteArrayOf(1)))))
        )
        assertEquals(expected, observed)
    }

    @Test
    fun testOtherTypesUseToString() {
        val body = object {
            override fun toString(): String = "custom"
        }
        assertEquals("custom", normalizeLogBody(body))
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.StringValue("custom"))),
            normalizeLogBody(listOf(body))
        )
    }

    @Test
    fun testCyclicCollectionIsTruncated() {
        val list = mutableListOf<Any>("a")
        list.add(list)
        val expected = AnyValue.ListValue(listOf(AnyValue.StringValue("a"), AnyValue.NullValue))
        assertEquals(expected, normalizeLogBody(list))
    }

    @Test
    fun testDeepNestingIsTruncated() {
        var body: Any = "leaf"
        repeat(100) { body = listOf(body) }
        var observed = normalizeLogBody(body)
        var depth = 0
        while (observed is AnyValue.ListValue) {
            observed = observed.values.single()
            depth++
        }
        assertEquals(16, depth)
        assertSame(AnyValue.NullValue, observed)
    }

    @Test
    fun testThrowingToStringPropagates() {
        val body = object {
            override fun toString(): String = error("boom")
        }
        assertFailsWith<IllegalStateException> { normalizeLogBody(body) }
    }
}
