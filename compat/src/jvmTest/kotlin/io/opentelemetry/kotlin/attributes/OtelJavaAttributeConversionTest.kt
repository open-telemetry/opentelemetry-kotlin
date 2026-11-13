package io.opentelemetry.kotlin.attributes

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Value
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OtelJavaAttributeConversionTest {

    @Test
    fun `every key type converts to its matching case`() {
        val cases = listOf(
            AttributeKey.stringKey("a") to "s" to AttributeValue.Str("s"),
            AttributeKey.booleanKey("a") to true to AttributeValue.Bool(true),
            AttributeKey.longKey("a") to 1L to AttributeValue.Long(1),
            AttributeKey.doubleKey("a") to 1.0 to AttributeValue.Double(1.0),
            AttributeKey.stringArrayKey("a") to listOf("s") to AttributeValue.StrList(listOf("s")),
            AttributeKey.booleanArrayKey("a") to listOf(true) to AttributeValue.BoolList(listOf(true)),
            AttributeKey.longArrayKey("a") to listOf(1L) to AttributeValue.LongList(listOf(1L)),
            AttributeKey.doubleArrayKey("a") to listOf(1.0) to AttributeValue.DoubleList(listOf(1.0)),
            AttributeKey.valueKey("a") to Value.of(1L) to AttributeValue.Value(AnyValue.LongValue(1L)),
            AttributeKey.valueKey("a") to Value.of(byteArrayOf(1)) to
                AttributeValue.Value(AnyValue.BytesValue(byteArrayOf(1))),
        )
        cases.forEach { (input, expected) ->
            val (key, value) = input
            assertEquals(expected, key.toAttributeValue(value), "key type ${key.type}, value $value")
        }
    }

    @Test
    fun `empty list keeps the key's list type`() {
        val value = AttributeKey.doubleArrayKey("a").toAttributeValue(emptyList<Double>())
        assertEquals(AttributeValue.DoubleList(emptyList()), value)
    }

    @Test
    fun `value that does not match the key type is ignored`() {
        assertNull(AttributeKey.longKey("a").toAttributeValue("s"))
        assertNull(AttributeKey.doubleKey("a").toAttributeValue(1L))
        assertNull(AttributeKey.stringArrayKey("a").toAttributeValue(listOf("s", 1L)))
        assertNull(AttributeKey.valueKey("a").toAttributeValue("s"))

        val attrs = CompatAttributesModel().apply { setOtelJavaAttribute(AttributeKey.longKey("a"), "s") }
        assertEquals(emptyMap(), attrs.attributes)
    }

    @Test
    fun `setOtelJavaAttributes sets every attribute`() {
        val attrs = OtelJavaAttributes.builder()
            .put("s", "v")
            .put("d", 2.0)
            .build()
        val model = CompatAttributesModel().apply { setOtelJavaAttributes(attrs) }
        assertEquals(mapOf("s" to "v", "d" to 2.0), model.attributes)
    }

    @Test
    fun `setAttributeMap infers types`() {
        val model = CompatAttributesModel().apply { setAttributeMap(mapOf("i" to 1, "d" to 1.0, "l" to listOf(1.0))) }
        assertEquals(mapOf("i" to 1L, "d" to 1.0, "l" to listOf(1.0)), model.attributes)
    }
}
