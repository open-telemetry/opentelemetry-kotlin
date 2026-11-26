package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class AttributeValueTest {

    private val bytes = byteArrayOf(1, 2, 3)

    private val allCases: List<AttributeValue> = listOf(
        AttributeValue.Str("s"),
        AttributeValue.Bool(true),
        AttributeValue.Long(1),
        AttributeValue.Double(1.5),
        AttributeValue.StrList(listOf("a", "b")),
        AttributeValue.BoolList(listOf(true, false)),
        AttributeValue.LongList(listOf(1, 2)),
        AttributeValue.DoubleList(listOf(1.5, 2.5)),
        AttributeValue.Value(AnyValue.BytesValue(bytes)),
        AttributeValue.Value(AnyValue.ListValue(listOf(AnyValue.StringValue("x")))),
    )

    @Test
    fun testRoundTripThroughPublic() {
        allCases.forEach {
            assertEquals(it, AttributeValue.fromPublic(it.toAny()))
        }
    }

    @Test
    fun testToAny() {
        assertEquals("s", AttributeValue.Str("s").toAny())
        assertEquals(true, AttributeValue.Bool(true).toAny())
        assertEquals(1L, AttributeValue.Long(1).toAny())
        assertEquals(1.5, AttributeValue.Double(1.5).toAny())
        assertEquals(listOf("a"), AttributeValue.StrList(listOf("a")).toAny())
        assertEquals(listOf(true), AttributeValue.BoolList(listOf(true)).toAny())
        assertEquals(listOf(1L), AttributeValue.LongList(listOf(1)).toAny())
        assertEquals(listOf(1.5), AttributeValue.DoubleList(listOf(1.5)).toAny())
        assertEquals(AnyValue.BytesValue(bytes), AttributeValue.Value(AnyValue.BytesValue(bytes)).toAny())
    }

    @Test
    fun testApplyToCallsMatchingSetter() {
        val expected = listOf(
            "setStringAttribute",
            "setBooleanAttribute",
            "setLongAttribute",
            "setDoubleAttribute",
            "setStringListAttribute",
            "setBooleanListAttribute",
            "setLongListAttribute",
            "setDoubleListAttribute",
            "setByteArrayAttribute",
            "setAnyValueAttribute",
        )
        allCases.zip(expected).forEach { (value, setter) ->
            val mutator = RecordingAttributesMutator()
            value.applyTo("key", mutator)
            assertEquals(listOf(setter to "key"), mutator.calls, "applyTo for $value")
            assertEquals(mapOf("key" to value.toAny()), mutator.values.toMap().mapValues { it.value.toAnyForm() })
        }
    }

    @Test
    fun testBytesEqualityIsContentBased() {
        val a = AttributeValue.Value(AnyValue.BytesValue(byteArrayOf(1, 2)))
        val b = AttributeValue.Value(AnyValue.BytesValue(byteArrayOf(1, 2)))
        val c = AttributeValue.Value(AnyValue.BytesValue(byteArrayOf(1, 3)))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun testEqualityIsTypeAware() {
        assertNotEquals<AttributeValue>(AttributeValue.Long(1), AttributeValue.Double(1.0))
        assertNotEquals<AttributeValue>(AttributeValue.StrList(emptyList()), AttributeValue.LongList(emptyList()))
        assertEquals(AttributeValue.StrList(listOf("a")), AttributeValue.StrList(listOf("a")))
        assertEquals(AttributeValue.StrList(listOf("a")).hashCode(), AttributeValue.StrList(listOf("a")).hashCode())
    }

    @Test
    fun testFromPublicNumbers() {
        assertEquals(AttributeValue.Double(1.0), AttributeValue.fromPublic(1.0))
        assertEquals(AttributeValue.Long(1), AttributeValue.fromPublic(1L))
        assertEquals(AttributeValue.Double(1.5), AttributeValue.fromPublic(1.5f))
        assertEquals(AttributeValue.DoubleList(listOf(1.0, 2.0)), AttributeValue.fromPublic(listOf(1.0, 2.0)))
        assertEquals(AttributeValue.LongList(listOf(1, 2)), AttributeValue.fromPublic(listOf(1L, 2L)))
        assertEquals(AttributeValue.DoubleList(listOf(1.0, 2.5)), AttributeValue.fromPublic(listOf(1L, 2.5f)))
    }

    @Test
    fun testFromPublicInt() {
        // JS can't distinguish an Int from a Double at runtime, so an Int is a Double there.
        val result = AttributeValue.fromPublic(1)
        assertTrue(result == AttributeValue.Long(1) || result == AttributeValue.Double(1.0), "$result")
    }

    @Test
    fun testFromPublicPrimitives() {
        assertEquals(AttributeValue.Str("s"), AttributeValue.fromPublic("s"))
        assertEquals(AttributeValue.Bool(false), AttributeValue.fromPublic(false))
        val existing = AttributeValue.Str("s")
        assertSame(existing, AttributeValue.fromPublic(existing))
        val anyValue = AnyValue.MapValue(mapOf("k" to AnyValue.LongValue(1)))
        assertEquals(AttributeValue.Value(anyValue), AttributeValue.fromPublic(anyValue))
    }

    @Test
    fun testFromPublicCopiesByteArray() {
        val source = byteArrayOf(1, 2)
        val value = AttributeValue.fromPublic(source) as AttributeValue.Value
        source[0] = 9
        assertContentEquals(byteArrayOf(1, 2), (value.value as AnyValue.BytesValue).value)
    }

    @Test
    fun testFromPublicLists() {
        assertEquals(AttributeValue.StrList(emptyList()), AttributeValue.fromPublic(emptyList<Long>()))
        assertEquals(
            AttributeValue.Value(
                AnyValue.ListValue(
                    listOf(
                        AnyValue.StringValue("a"),
                        AnyValue.BoolValue(true),
                        AnyValue.NullValue,
                    )
                )
            ),
            AttributeValue.fromPublic(listOf("a", true, null))
        )
        assertEquals(AttributeValue.BoolList(listOf(true)), AttributeValue.fromPublic(setOf(true)))
        assertEquals(AttributeValue.StrList(listOf("a")), AttributeValue.fromPublic(arrayOf("a")))
    }

    @Test
    fun testFromPublicPrimitiveArrays() {
        assertEquals(AttributeValue.BoolList(listOf(true, false)), AttributeValue.fromPublic(booleanArrayOf(true, false)))
        assertEquals(AttributeValue.LongList(listOf(10L, 20L)), AttributeValue.fromPublic(longArrayOf(10L, 20L)))
        assertEquals(AttributeValue.LongList(listOf(1L, 2L)), AttributeValue.fromPublic(intArrayOf(1, 2)))
        assertEquals(AttributeValue.LongList(listOf(3L, 4L)), AttributeValue.fromPublic(shortArrayOf(3.toShort(), 4.toShort())))
        assertEquals(AttributeValue.DoubleList(listOf(1.5, 2.5)), AttributeValue.fromPublic(doubleArrayOf(1.5, 2.5)))
        assertEquals(AttributeValue.DoubleList(listOf(3.5, 4.5)), AttributeValue.fromPublic(floatArrayOf(3.5f, 4.5f)))
        assertEquals(AttributeValue.StrList(listOf("x", "y")), AttributeValue.fromPublic(charArrayOf('x', 'y')))
    }

    @Test
    fun testFromPublicMaps() {
        val map = mapOf("k" to "v", 123 to 456L, "nullKey" to null)
        val expected = AttributeValue.Value(
            AnyValue.MapValue(
                mapOf(
                    "k" to AnyValue.StringValue("v"),
                    "123" to AnyValue.LongValue(456L),
                    "nullKey" to AnyValue.NullValue,
                )
            )
        )
        assertEquals(expected, AttributeValue.fromPublic(map))
    }

    @Test
    fun testFromPublicMapKeyCollisions() {
        val map = mapOf(1 to "numeric", "1" to "text")
        val expected = AttributeValue.Value(
            AnyValue.MapValue(
                mapOf(
                    "1" to AnyValue.StringValue("numeric"),
                    "1_1" to AnyValue.StringValue("text"),
                )
            )
        )
        assertEquals(expected, AttributeValue.fromPublic(map))
    }

    @Test
    fun testFromPublicMapKeyCollisionsWithMultipleKeys() {
        val map = linkedMapOf<Any, String>(
            CollidingKey(1) to "first",
            CollidingKey(2) to "second",
            CollidingKey(3) to "third",
        )
        val expected = AttributeValue.Value(
            AnyValue.MapValue(
                mapOf(
                    "same" to AnyValue.StringValue("first"),
                    "same_1" to AnyValue.StringValue("second"),
                    "same_2" to AnyValue.StringValue("third"),
                )
            )
        )
        assertEquals(expected, AttributeValue.fromPublic(map))
    }

    @Test
    fun testFromPublicPreservesDoubleValueForWholeValuedDoubleAndNegativeZero() {
        val map = mapOf("whole" to 1.0, "negZero" to -0.0)
        val expected = AttributeValue.Value(
            AnyValue.MapValue(
                mapOf(
                    "whole" to AnyValue.DoubleValue(1.0),
                    "negZero" to AnyValue.DoubleValue(-0.0),
                )
            )
        )
        assertEquals(expected, AttributeValue.fromPublic(map))
    }

    @Test
    fun testFromPublicNestedMapsDepthBoundary() {
        var current: Any = "leaf"
        repeat(15) {
            current = mapOf("next" to current)
        }
        val attr15 = AttributeValue.fromPublic(current) as AttributeValue.Value
        var node: AnyValue = attr15.value
        repeat(15) {
            val mapValue = node as AnyValue.MapValue
            node = checkNotNull(mapValue.value["next"])
        }
        assertEquals(AnyValue.StringValue("leaf"), node)

        var deeper: Any = "leaf"
        repeat(16) {
            deeper = mapOf("next" to deeper)
        }
        val attr16 = AttributeValue.fromPublic(deeper) as AttributeValue.Value
        var deepNode: AnyValue = attr16.value
        repeat(16) {
            val mapValue = deepNode as AnyValue.MapValue
            deepNode = checkNotNull(mapValue.value["next"])
        }
        assertEquals(AnyValue.NullValue, deepNode)
    }

    @Test
    fun testFromPublicListsWithBytes() {
        val b1 = byteArrayOf(1, 2)
        val b2 = byteArrayOf(3, 4)
        val list = listOf(b1, b2)
        val expected = AttributeValue.Value(
            AnyValue.ListValue(
                listOf(
                    AnyValue.BytesValue(b1),
                    AnyValue.BytesValue(b2),
                )
            )
        )
        assertEquals(expected, AttributeValue.fromPublic(list))
    }

    @Test
    fun testFromPublicSelfReferentialRootList() {
        val list = ArrayList<Any?>()
        list.add(list)
        val expected = AttributeValue.Value(AnyValue.ListValue(listOf(AnyValue.NullValue)))
        assertEquals(expected, AttributeValue.fromPublic(list))
    }

    @Test
    fun testFromPublicSelfReferentialRootArray() {
        val arr = arrayOfNulls<Any>(1)
        arr[0] = arr
        val expected = AttributeValue.Value(AnyValue.ListValue(listOf(AnyValue.NullValue)))
        assertEquals(expected, AttributeValue.fromPublic(arr))
    }

    @Test
    fun testFromPublicSelfReferentialRootListWithNullAndElements() {
        val list = ArrayList<Any?>()
        list.add("entry")
        list.add(null)
        list.add(list)
        val expected = AttributeValue.Value(
            AnyValue.ListValue(
                listOf(
                    AnyValue.StringValue("entry"),
                    AnyValue.NullValue,
                    AnyValue.NullValue,
                )
            )
        )
        assertEquals(expected, AttributeValue.fromPublic(list))
    }

    @Test
    fun testFromPublicOther() {
        val obj = object {
            override fun toString(): String = "custom"
        }
        assertEquals(AttributeValue.Str("custom"), AttributeValue.fromPublic(obj))
    }

    @Test
    fun testListsAreCopiedOnConstruction() {
        val strings = mutableListOf("a")
        val bools = mutableListOf(true)
        val longs = mutableListOf(1L)
        val doubles = mutableListOf(1.5)
        val values = listOf(
            AttributeValue.StrList(strings),
            AttributeValue.BoolList(bools),
            AttributeValue.LongList(longs),
            AttributeValue.DoubleList(doubles),
        )
        strings.add("b")
        bools.add(false)
        longs.add(2)
        doubles.add(2.5)
        assertEquals(
            listOf<Any>(listOf("a"), listOf(true), listOf(1L), listOf(1.5)),
            values.map { it.toAny() }
        )
    }

    @Test
    fun testStringsTruncatedOnConstruction() {
        assertEquals("ab", AttributeValue.Str("abc", 2).value)
        assertEquals("abc", AttributeValue.Str("abc", 3).value)
        assertEquals("abc", AttributeValue.Str("abc").value)
        assertEquals("", AttributeValue.Str("abc", 0).value)
        assertEquals(listOf("a", "c"), AttributeValue.StrList(listOf("ab", "c"), 1).value)
        assertEquals(AnyValue.StringValue("a"), AttributeValue.Value(AnyValue.StringValue("ab"), 1).value)
    }

    @Test
    fun testBytesTruncatedAndCopiedOnConstruction() {
        val source = byteArrayOf(1, 2, 3)
        val truncated = AttributeValue.Value(AnyValue.BytesValue(source), 2).value as AnyValue.BytesValue
        val copied = AttributeValue.Value(AnyValue.BytesValue(source)).value as AnyValue.BytesValue
        source[0] = 9
        assertContentEquals(byteArrayOf(1, 2), truncated.value)
        assertContentEquals(byteArrayOf(1, 2, 3), copied.value)
    }

    @Test
    fun testTruncationDoesNotSplitSurrogatePairs() {
        // 😀 is one code point made of two UTF-16 code units
        assertEquals("ab😀", AttributeValue.Str("ab😀cd", 3).value)
        assertEquals(listOf("😀😀"), AttributeValue.StrList(listOf("😀😀😀"), 2).value)
        val nested = AttributeValue.Value(AnyValue.ListValue(listOf(AnyValue.StringValue("a😀b"))), 2).value
        assertEquals(AnyValue.ListValue(listOf(AnyValue.StringValue("a😀"))), nested)
    }

    @Test
    fun testNestedValuesTruncatedAndCopiedOnConstruction() {
        val source = byteArrayOf(1, 2, 3)
        val items = mutableListOf<AnyValue>(AnyValue.StringValue("abc"), AnyValue.BytesValue(source))
        val value = AttributeValue.Value(AnyValue.MapValue(mapOf("list" to AnyValue.ListValue(items))), 2)
        items.add(AnyValue.StringValue("extra"))
        source[0] = 9
        val stored = ((value.value as AnyValue.MapValue).value["list"] as AnyValue.ListValue).value
        assertEquals(2, stored.size)
        assertEquals(AnyValue.StringValue("ab"), stored[0])
        assertContentEquals(byteArrayOf(1, 2), (stored[1] as AnyValue.BytesValue).value)
    }

    @Test
    fun testSelfReferentialValueReplacedWithNull() {
        val items = mutableListOf<AnyValue>(AnyValue.StringValue("a"))
        val list = AnyValue.ListValue(items)
        items.add(list)
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.StringValue("a"), AnyValue.NullValue)),
            AttributeValue.Value(list).value,
        )
    }

    @Test
    fun testTypedAttributesOrConvert() {
        val typed = mapOf<String, AttributeValue>("d" to AttributeValue.Double(1.0))
        val typedContainer = object : TypedAttributeContainer {
            override val typedAttributes = typed
            override val attributes: Map<String, Any> = mapOf("d" to 1.0)
        }
        assertSame(typed, typedContainer.typedAttributesOrConvert())

        val untyped = object : AttributeContainer {
            override val attributes: Map<String, Any> = mapOf("s" to "v", "bytes" to byteArrayOf(1))
        }
        assertEquals(
            mapOf(
                "s" to AttributeValue.Str("v"),
                "bytes" to AttributeValue.Value(AnyValue.BytesValue(byteArrayOf(1))),
            ),
            untyped.typedAttributesOrConvert(),
        )
    }

    @Test
    fun testNegativeLimitDoesNotTruncate() {
        assertEquals("abc", AttributeValue.Str("abc", -1).value)
        assertEquals(listOf("abc"), AttributeValue.StrList(listOf("abc"), -5).value)
    }

    @Test
    fun testToString() {
        assertEquals("Str(value=s)", AttributeValue.Str("s").toString())
        assertEquals("LongList(value=[1, 2])", AttributeValue.LongList(listOf(1, 2)).toString())
    }

    private fun Any.toAnyForm(): Any = when (this) {
        is ByteArray -> AnyValue.BytesValue(this)
        else -> this
    }

    private class CollidingKey(val id: Int) {
        override fun toString(): String = "same"
    }

    private class RecordingAttributesMutator : AttributesMutator {
        val calls = mutableListOf<Pair<String, String>>()
        val values = mutableMapOf<String, Any>()

        private fun record(setter: String, key: String, value: Any) {
            calls.add(setter to key)
            values[key] = value
        }

        override fun setBooleanAttribute(key: String, value: Boolean) = record("setBooleanAttribute", key, value)
        override fun setStringAttribute(key: String, value: String) = record("setStringAttribute", key, value)
        override fun setLongAttribute(key: String, value: Long) = record("setLongAttribute", key, value)
        override fun setDoubleAttribute(key: String, value: Double) = record("setDoubleAttribute", key, value)
        override fun setBooleanListAttribute(key: String, value: List<Boolean>) =
            record("setBooleanListAttribute", key, value)
        override fun setStringListAttribute(key: String, value: List<String>) =
            record("setStringListAttribute", key, value)
        override fun setLongListAttribute(key: String, value: List<Long>) = record("setLongListAttribute", key, value)
        override fun setDoubleListAttribute(key: String, value: List<Double>) =
            record("setDoubleListAttribute", key, value)
        override fun setByteArrayAttribute(key: String, value: ByteArray) = record("setByteArrayAttribute", key, value)
        override fun setAnyValueAttribute(key: String, value: AnyValue) = record("setAnyValueAttribute", key, value)
    }
}
