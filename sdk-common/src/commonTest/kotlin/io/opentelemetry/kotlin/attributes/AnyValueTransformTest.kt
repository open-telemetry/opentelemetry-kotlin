package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class AnyValueTransformTest {

    @Test
    fun testScalarsAreReturnedAsIs() {
        val values = listOf(
            AnyValue.NullValue,
            AnyValue.StringValue("s"),
            AnyValue.BoolValue(true),
            AnyValue.LongValue(1),
            AnyValue.DoubleValue(1.5),
        )
        values.forEach { assertSame(it, it.transform()) }
    }

    @Test
    fun testBytesAreCopied() {
        val bytes = byteArrayOf(1, 2)
        val original = AnyValue.BytesValue(bytes)
        val observed = original.transform() as AnyValue.BytesValue
        bytes[0] = 9
        assertNotSame(original, observed)
        assertEquals(AnyValue.BytesValue(byteArrayOf(1, 2)), observed)
    }

    @Test
    fun testCollectionsAreDeepCopied() {
        val bytes = byteArrayOf(1)
        val list = mutableListOf<AnyValue>(AnyValue.BytesValue(bytes))
        val map = mutableMapOf<String, AnyValue>("k" to AnyValue.ListValue(list))
        val observed = AnyValue.MapValue(map).transform()
        bytes[0] = 9
        list.add(AnyValue.NullValue)
        map["k2"] = AnyValue.NullValue

        val expected = AnyValue.MapValue(
            mapOf("k" to AnyValue.ListValue(listOf(AnyValue.BytesValue(byteArrayOf(1)))))
        )
        assertEquals(expected, observed)
    }

    @Test
    fun testSelfReferentialListIsTruncated() {
        val values = mutableListOf<AnyValue>(AnyValue.StringValue("a"))
        val list = AnyValue.ListValue(values)
        values.add(list)

        val expected = AnyValue.ListValue(listOf(AnyValue.StringValue("a"), AnyValue.NullValue))
        assertEquals(expected, list.transform())
    }

    @Test
    fun testSelfReferentialMapIsTruncated() {
        val values = mutableMapOf<String, AnyValue>()
        val map = AnyValue.MapValue(values)
        values["self"] = AnyValue.ListValue(listOf(map))

        val expected = AnyValue.MapValue(mapOf("self" to AnyValue.ListValue(listOf(AnyValue.NullValue))))
        assertEquals(expected, map.transform())
    }

    @Test
    fun testRepeatedSiblingIsNotACycle() {
        val shared = AnyValue.ListValue(listOf(AnyValue.LongValue(1)))
        val value = AnyValue.ListValue(listOf(shared, shared))
        assertEquals(value, value.transform())
    }

    @Test
    fun testDeepNestingIsTruncated() {
        var value: AnyValue = AnyValue.StringValue("leaf")
        repeat(100) { value = AnyValue.ListValue(listOf(value)) }
        assertEquals(16, value.transform().listDepth())
    }

    @Test
    fun testCustomMaxDepth() {
        val value = AnyValue.ListValue(listOf(AnyValue.ListValue(listOf(AnyValue.StringValue("leaf")))))
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.ListValue(listOf(AnyValue.NullValue)))),
            value.transform(maxDepth = 2)
        )
        assertSame(AnyValue.NullValue, value.transform(maxDepth = 0))
    }

    @Test
    fun testLeafAppliesToNestedStringsAndBytes() {
        val value = AnyValue.MapValue(
            mapOf(
                "str" to AnyValue.StringValue("abcdef"),
                "list" to AnyValue.ListValue(
                    listOf(AnyValue.StringValue("ghijkl"), AnyValue.BytesValue(byteArrayOf(1, 2, 3, 4)))
                ),
                "map" to AnyValue.MapValue(mapOf("bytes" to AnyValue.BytesValue(byteArrayOf(5, 6, 7)))),
                "long" to AnyValue.LongValue(123456),
            )
        )
        val expected = AnyValue.MapValue(
            mapOf(
                "str" to AnyValue.StringValue("ab"),
                "list" to AnyValue.ListValue(
                    listOf(AnyValue.StringValue("gh"), AnyValue.BytesValue(byteArrayOf(1, 2)))
                ),
                "map" to AnyValue.MapValue(mapOf("bytes" to AnyValue.BytesValue(byteArrayOf(5, 6)))),
                "long" to AnyValue.LongValue(123456),
            )
        )
        assertEquals(expected, value.transform(leaf = ::truncateTo2))
    }

    private fun truncateTo2(value: AnyValue): AnyValue = when (value) {
        is AnyValue.StringValue -> AnyValue.StringValue(value.value.take(2))
        is AnyValue.BytesValue -> AnyValue.BytesValue(value.value.copyOf(2))
        else -> value
    }

    private fun AnyValue.listDepth(): Int {
        var observed = this
        var depth = 0
        while (observed is AnyValue.ListValue) {
            observed = observed.values.single()
            depth++
        }
        assertSame(AnyValue.NullValue, observed)
        return depth
    }
}
