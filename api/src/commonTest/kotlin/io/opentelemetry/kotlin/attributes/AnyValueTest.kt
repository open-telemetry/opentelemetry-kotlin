@file:OptIn(io.opentelemetry.kotlin.ExperimentalApi::class)

package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class AnyValueTest {

    @Test
    fun nullValueIsSingleton() {
        assertSame(AnyValue.NullValue, AnyValue.NullValue)
        assertEquals(AnyValue.NullValue, AnyValue.NullValue)
        assertEquals(AnyValue.NullValue.hashCode(), AnyValue.NullValue.hashCode())
    }

    @Test
    fun nullValueNotEqualToOtherTypes() {
        assertNotEquals<Any>(AnyValue.NullValue, AnyValue.StringValue(""))
        assertNotEquals<Any>(AnyValue.NullValue, AnyValue.LongValue(0L))
        assertNotEquals<Any>(AnyValue.NullValue, AnyValue.BoolValue(false))
        assertNotEquals<Any>(AnyValue.NullValue, AnyValue.ListValue(emptyList()))
        assertNotEquals<Any>(AnyValue.NullValue, AnyValue.MapValue(emptyMap()))
    }

    @Test
    fun stringValueExposesValue() {
        assertEquals("hello", AnyValue.StringValue("hello").value)
    }

    @Test
    fun emptyStringValueIsPreserved() {
        val empty = AnyValue.StringValue("")
        assertEquals("", empty.value)
        assertEquals(AnyValue.StringValue(""), empty)
    }

    @Test
    fun stringValueEqualityAndHashCode() {
        val a = AnyValue.StringValue("x")
        val b = AnyValue.StringValue("x")
        val c = AnyValue.StringValue("y")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun boolValueExposesValue() {
        assertEquals(true, AnyValue.BoolValue(true).value)
        assertEquals(false, AnyValue.BoolValue(false).value)
    }

    @Test
    fun boolValueEqualityAndHashCode() {
        val t1 = AnyValue.BoolValue(true)
        val t2 = AnyValue.BoolValue(true)
        val f = AnyValue.BoolValue(false)
        assertEquals(t1, t2)
        assertEquals(t1.hashCode(), t2.hashCode())
        assertNotEquals(t1, f)
    }

    @Test
    fun longValueExposesValue() {
        assertEquals(42L, AnyValue.LongValue(42L).value)
    }

    @Test
    fun longValueZeroIsPreserved() {
        assertEquals(0L, AnyValue.LongValue(0L).value)
        assertEquals(AnyValue.LongValue(0L), AnyValue.LongValue(0L))
    }

    @Test
    fun longValueExtremes() {
        assertEquals(Long.MAX_VALUE, AnyValue.LongValue(Long.MAX_VALUE).value)
        assertEquals(Long.MIN_VALUE, AnyValue.LongValue(Long.MIN_VALUE).value)
    }

    @Test
    fun longValueEqualityAndHashCode() {
        val a = AnyValue.LongValue(7L)
        val b = AnyValue.LongValue(7L)
        val c = AnyValue.LongValue(8L)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun doubleValueExposesValue() {
        assertEquals(3.14, AnyValue.DoubleValue(3.14).value)
    }

    @Test
    fun doubleValueZeroIsPreserved() {
        assertEquals(0.0, AnyValue.DoubleValue(0.0).value)
        assertEquals(AnyValue.DoubleValue(0.0), AnyValue.DoubleValue(0.0))
    }

    @Test
    fun doubleValueSpecialFloatingPoint() {
        assertTrue(AnyValue.DoubleValue(Double.NaN).value.isNaN())
        assertEquals(
            Double.POSITIVE_INFINITY,
            AnyValue.DoubleValue(Double.POSITIVE_INFINITY).value
        )
        assertEquals(
            Double.NEGATIVE_INFINITY,
            AnyValue.DoubleValue(Double.NEGATIVE_INFINITY).value
        )
    }

    @Test
    fun doubleValueEqualityAndHashCode() {
        val a = AnyValue.DoubleValue(1.5)
        val b = AnyValue.DoubleValue(1.5)
        val c = AnyValue.DoubleValue(2.5)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }

    @Test
    fun bytesValueExposesValue() {
        val bytes = byteArrayOf(0x01, 0x02, 0x03)
        assertTrue(AnyValue.BytesValue(bytes).value.contentEquals(bytes))
    }

    @Test
    fun bytesValueEmptyIsPreserved() {
        val empty = AnyValue.BytesValue(byteArrayOf())
        assertEquals(0, empty.value.size)
        assertEquals(AnyValue.BytesValue(byteArrayOf()), empty)
    }

    @Test
    fun bytesValueUsesContentEqualityNotIdentity() {
        val a = AnyValue.BytesValue(byteArrayOf(1, 2, 3))
        val b = AnyValue.BytesValue(byteArrayOf(1, 2, 3))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun bytesValueDifferentContentNotEqual() {
        val a = AnyValue.BytesValue(byteArrayOf(1, 2, 3))
        val b = AnyValue.BytesValue(byteArrayOf(1, 2, 4))
        assertNotEquals(a, b)
    }

    @Test
    fun bytesValueDifferentLengthNotEqual() {
        val a = AnyValue.BytesValue(byteArrayOf(1, 2))
        val b = AnyValue.BytesValue(byteArrayOf(1, 2, 0))
        assertNotEquals(a, b)
    }

    @Test
    fun bytesValueEqualsItself() {
        val a = AnyValue.BytesValue(byteArrayOf(1, 2, 3))
        assertEquals(a, a)
    }

    @Test
    fun bytesValueNotEqualToOtherTypes() {
        val bytes = AnyValue.BytesValue(byteArrayOf(1, 2, 3))
        assertNotEquals<Any>(bytes, AnyValue.StringValue("123"))
        assertNotEquals<Any>(bytes, "raw bytes")
    }

    @Test
    fun listValueExposesValues() {
        val list = listOf(
            AnyValue.StringValue("a"),
            AnyValue.LongValue(1L)
        )
        assertEquals(list, AnyValue.ListValue(list).values)
    }

    @Test
    fun listValueEmptyIsPreserved() {
        val empty = AnyValue.ListValue(emptyList())
        assertEquals(0, empty.values.size)
        assertEquals(AnyValue.ListValue(emptyList()), empty)
    }

    @Test
    fun listValueHomogeneousEquality() {
        val a = AnyValue.ListValue(
            listOf(AnyValue.LongValue(1L), AnyValue.LongValue(2L))
        )
        val b = AnyValue.ListValue(
            listOf(AnyValue.LongValue(1L), AnyValue.LongValue(2L))
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun listValueOrderMatters() {
        val a = AnyValue.ListValue(
            listOf(AnyValue.LongValue(1L), AnyValue.LongValue(2L))
        )
        val b = AnyValue.ListValue(
            listOf(AnyValue.LongValue(2L), AnyValue.LongValue(1L))
        )
        assertNotEquals(a, b)
    }

    @Test
    fun listValueHeterogeneousEquality() {
        val a = AnyValue.ListValue(
            listOf(
                AnyValue.StringValue("s"),
                AnyValue.LongValue(1L),
                AnyValue.DoubleValue(2.0),
                AnyValue.BoolValue(true)
            )
        )
        val b = AnyValue.ListValue(
            listOf(
                AnyValue.StringValue("s"),
                AnyValue.LongValue(1L),
                AnyValue.DoubleValue(2.0),
                AnyValue.BoolValue(true)
            )
        )
        assertEquals(a, b)
    }

    @Test
    fun listValueNestedArraysEquality() {
        val a = AnyValue.ListValue(
            listOf(
                AnyValue.ListValue(listOf(AnyValue.LongValue(1L))),
                AnyValue.ListValue(listOf(AnyValue.LongValue(2L)))
            )
        )
        val b = AnyValue.ListValue(
            listOf(
                AnyValue.ListValue(listOf(AnyValue.LongValue(1L))),
                AnyValue.ListValue(listOf(AnyValue.LongValue(2L)))
            )
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun listValueContainingBytesUsesContentEquality() {
        val a = AnyValue.ListValue(
            listOf(AnyValue.BytesValue(byteArrayOf(1, 2)))
        )
        val b = AnyValue.ListValue(
            listOf(AnyValue.BytesValue(byteArrayOf(1, 2)))
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun mapValueExposesValues() {
        val map = mapOf(
            "name" to AnyValue.StringValue("alice"),
            "age" to AnyValue.LongValue(30L)
        )
        assertEquals(map, AnyValue.MapValue(map).values)
    }

    @Test
    fun mapValueEmptyIsPreserved() {
        val empty = AnyValue.MapValue(emptyMap())
        assertEquals(0, empty.values.size)
        assertEquals(AnyValue.MapValue(emptyMap()), empty)
    }

    @Test
    fun mapValueEquality() {
        val a = AnyValue.MapValue(
            mapOf("k" to AnyValue.LongValue(1L))
        )
        val b = AnyValue.MapValue(
            mapOf("k" to AnyValue.LongValue(1L))
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun mapValueDifferentKeysNotEqual() {
        val a = AnyValue.MapValue(
            mapOf("a" to AnyValue.LongValue(1L))
        )
        val b = AnyValue.MapValue(
            mapOf("b" to AnyValue.LongValue(1L))
        )
        assertNotEquals(a, b)
    }

    @Test
    fun mapValueKeysAreCaseSensitive() {
        val a = AnyValue.MapValue(
            mapOf("Name" to AnyValue.StringValue("alice"))
        )
        val b = AnyValue.MapValue(
            mapOf("name" to AnyValue.StringValue("alice"))
        )
        assertNotEquals(a, b)
    }

    @Test
    fun mapValueEqualityIndependentOfInsertionOrder() {
        val a = AnyValue.MapValue(
            linkedMapOf(
                "first" to AnyValue.LongValue(1L),
                "second" to AnyValue.LongValue(2L)
            )
        )
        val b = AnyValue.MapValue(
            linkedMapOf(
                "second" to AnyValue.LongValue(2L),
                "first" to AnyValue.LongValue(1L)
            )
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun mapValueNestedEquality() {
        val a = AnyValue.MapValue(
            mapOf(
                "outer" to AnyValue.MapValue(
                    mapOf("inner" to AnyValue.LongValue(7L))
                )
            )
        )
        val b = AnyValue.MapValue(
            mapOf(
                "outer" to AnyValue.MapValue(
                    mapOf("inner" to AnyValue.LongValue(7L))
                )
            )
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun mapValueWithListValuesEquality() {
        val a = AnyValue.MapValue(
            mapOf(
                "list" to AnyValue.ListValue(
                    listOf(AnyValue.StringValue("a"), AnyValue.StringValue("b"))
                )
            )
        )
        val b = AnyValue.MapValue(
            mapOf(
                "list" to AnyValue.ListValue(
                    listOf(AnyValue.StringValue("a"), AnyValue.StringValue("b"))
                )
            )
        )
        assertEquals(a, b)
    }

    @Test
    fun listValueWithMapValueEquality() {
        val a = AnyValue.ListValue(
            listOf(
                AnyValue.MapValue(mapOf("k" to AnyValue.LongValue(1L))),
                AnyValue.MapValue(mapOf("k" to AnyValue.LongValue(2L)))
            )
        )
        val b = AnyValue.ListValue(
            listOf(
                AnyValue.MapValue(mapOf("k" to AnyValue.LongValue(1L))),
                AnyValue.MapValue(mapOf("k" to AnyValue.LongValue(2L)))
            )
        )
        assertEquals(a, b)
    }

    @Test
    fun deeplyNestedStructureEquality() {
        val build = {
            AnyValue.MapValue(
                mapOf(
                    "data" to AnyValue.ListValue(
                        listOf(
                            AnyValue.MapValue(
                                mapOf(
                                    "id" to AnyValue.LongValue(1L),
                                    "tags" to AnyValue.ListValue(
                                        listOf(
                                            AnyValue.StringValue("a"),
                                            AnyValue.StringValue("b")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }
        assertEquals(build(), build())
        assertEquals(build().hashCode(), build().hashCode())
    }

    @Test
    fun differentVariantsAreNotEqual() {
        val variants = listOf(
            AnyValue.NullValue,
            AnyValue.StringValue("1"),
            AnyValue.BoolValue(true),
            AnyValue.LongValue(1L),
            AnyValue.DoubleValue(1.0),
            AnyValue.BytesValue(byteArrayOf(1)),
            AnyValue.ListValue(emptyList()),
            AnyValue.MapValue(emptyMap())
        )
        for (i in variants.indices) {
            for (j in variants.indices) {
                if (i != j) {
                    assertNotEquals(variants[i], variants[j])
                }
            }
        }
    }

    @Test
    fun variantsCanBeDiscriminatedByIsCheck() {
        val values: List<AnyValue> = listOf(
            AnyValue.NullValue,
            AnyValue.StringValue("s"),
            AnyValue.BoolValue(true),
            AnyValue.LongValue(1L),
            AnyValue.DoubleValue(1.0),
            AnyValue.BytesValue(byteArrayOf(1)),
            AnyValue.ListValue(emptyList()),
            AnyValue.MapValue(emptyMap())
        )
        val tags = values.map {
            when (it) {
                is AnyValue.NullValue -> "null"
                is AnyValue.StringValue -> "string"
                is AnyValue.BoolValue -> "bool"
                is AnyValue.LongValue -> "long"
                is AnyValue.DoubleValue -> "double"
                is AnyValue.BytesValue -> "bytes"
                is AnyValue.ListValue -> "list"
                is AnyValue.MapValue -> "map"
            }
        }
        assertEquals(
            listOf("null", "string", "bool", "long", "double", "bytes", "list", "map"),
            tags
        )
    }

    @Test
    fun toStringIncludesValueForPrimitive() {
        assertTrue(AnyValue.StringValue("hello").toString().contains("hello"))
        assertTrue(AnyValue.LongValue(42L).toString().contains("42"))
    }
}
