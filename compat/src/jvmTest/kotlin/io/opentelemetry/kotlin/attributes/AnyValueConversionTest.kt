package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaKeyValue
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.aliases.OtelJavaValueType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AnyValueConversionTest {

    @Test
    fun testAnyValuePrimitivesToJavaValue() {
        assertEquals(OtelJavaValue.of("s"), AnyValue.StringValue("s").toOtelJavaValue())
        assertEquals(OtelJavaValue.of(true), AnyValue.BoolValue(true).toOtelJavaValue())
        assertEquals(OtelJavaValue.of(5L), AnyValue.LongValue(5).toOtelJavaValue())
        assertEquals(OtelJavaValue.of(1.5), AnyValue.DoubleValue(1.5).toOtelJavaValue())
        assertEquals(OtelJavaValue.empty(), AnyValue.NullValue.toOtelJavaValue())
        assertEquals(OtelJavaValue.of(byteArrayOf(1, 2)), AnyValue.BytesValue(byteArrayOf(1, 2)).toOtelJavaValue())
    }

    @Test
    fun testAnyValueStructuredToJavaValue() {
        val body = AnyValue.MapValue(
            mapOf(
                "str" to AnyValue.StringValue("v"),
                "list" to AnyValue.ListValue(listOf(AnyValue.LongValue(1), AnyValue.NullValue)),
                "nested" to AnyValue.MapValue(mapOf("d" to AnyValue.DoubleValue(Double.NaN))),
            )
        )
        val observed = body.toOtelJavaValue()
        assertEquals(OtelJavaValueType.KEY_VALUE_LIST, observed.type)
        assertEquals("""{"str":"v","list":[1,null],"nested":{"d":"NaN"}}""", observed.asString())
    }

    @Test
    fun testRawKotlinTypesToJavaValue() {
        assertEquals(OtelJavaValue.of("s"), "s".toOtelJavaValue())
        assertEquals(OtelJavaValue.of(false), false.toOtelJavaValue())
        assertEquals(OtelJavaValue.of(5L), 5L.toOtelJavaValue())
        assertEquals(OtelJavaValue.of(5L), 5.toOtelJavaValue())
        assertEquals(OtelJavaValue.of(2.5), 2.5.toOtelJavaValue())
        assertEquals(OtelJavaValue.of(2.5), 2.5f.toOtelJavaValue())
    }

    @Test
    fun testRawStructuredTypesToJavaValue() {
        val map = mapOf("k" to listOf(1, "a"), "b" to byteArrayOf(1))
        val observed = map.toOtelJavaValue()
        assertEquals(OtelJavaValueType.KEY_VALUE_LIST, observed.type)
        assertEquals("""{"k":[1,"a"],"b":"AQ=="}""", observed.asString())
    }

    @Test
    fun testRawByteArrayToJavaValue() {
        val observed = byteArrayOf(1, 2).toOtelJavaValue()
        assertEquals(OtelJavaValueType.BYTES, observed.type)
        assertEquals(OtelJavaValue.of(byteArrayOf(1, 2)), observed)
    }

    @Test
    fun testOtherTypesFallBackToString() {
        val obj = object {
            override fun toString(): String = "custom"
        }
        assertEquals(OtelJavaValue.of("custom"), obj.toOtelJavaValue())
    }

    @Test
    fun testJavaValuePrimitivesToKotlinBody() {
        assertEquals("s", OtelJavaValue.of("s").toOtelKotlinBody())
        assertEquals(true, OtelJavaValue.of(true).toOtelKotlinBody())
        assertEquals(5L, OtelJavaValue.of(5L).toOtelKotlinBody())
        assertEquals(1.5, OtelJavaValue.of(1.5).toOtelKotlinBody())
        assertNull(OtelJavaValue.empty().toOtelKotlinBody())
    }

    @Test
    fun testJavaValueStructuredToKotlinBody() {
        assertEquals(
            AnyValue.BytesValue(byteArrayOf(1, 2)),
            OtelJavaValue.of(byteArrayOf(1, 2)).toOtelKotlinBody()
        )
        val value = OtelJavaValue.of(
            OtelJavaKeyValue.of("list", OtelJavaValue.of(OtelJavaValue.of("a"), OtelJavaValue.empty())),
            OtelJavaKeyValue.of("long", OtelJavaValue.of(3L)),
        )
        val expected = AnyValue.MapValue(
            mapOf(
                "list" to AnyValue.ListValue(listOf(AnyValue.StringValue("a"), AnyValue.NullValue)),
                "long" to AnyValue.LongValue(3),
            )
        )
        assertEquals(expected, value.toOtelKotlinBody())
    }

    @Test
    fun testRoundTrip() {
        val body = AnyValue.ListValue(
            listOf(
                AnyValue.BytesValue(byteArrayOf(9)),
                AnyValue.MapValue(mapOf("b" to AnyValue.BoolValue(false))),
            )
        )
        assertEquals(body, body.toOtelJavaValue().toOtelKotlinBody())
    }

    @Test
    fun testMismatchedJavaValueDegradesToNull() {
        val bogus = object : OtelJavaValue<Any> {
            override fun getType(): OtelJavaValueType = OtelJavaValueType.ARRAY
            override fun getValue(): Any = listOf("not a value", 1)
            override fun asString(): String = ""
        }
        assertEquals(AnyValue.ListValue(listOf(AnyValue.NullValue, AnyValue.NullValue)), bogus.toOtelKotlinBody())
    }
}
