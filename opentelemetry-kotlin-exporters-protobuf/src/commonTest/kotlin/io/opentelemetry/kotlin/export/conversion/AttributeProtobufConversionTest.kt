package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.proto.common.v1.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@OptIn(ExperimentalApi::class)
class AttributeProtobufConversionTest {

    @Test
    fun testEmptyConversion() {
        assertEquals(0, emptyMap<String, Any>().createKeyValues().size)
    }

    @Test
    fun testConversionOfValues() {
        assertEquals(createForValue("string").value_?.string_value, "string")
        assertEquals(createForValue(42L).value_?.int_value, 42L)
        assertEquals(createForValue(0.2).value_?.double_value, 0.2)
        assertEquals(createForValue(true).value_?.bool_value, true)
    }

    @Test
    fun testListConversion() {
        val createKeyValues = mapOf("key" to listOf("val", 32L)).createKeyValues()
        assertEquals(1, createKeyValues.size)
        assertEquals("key", createKeyValues[0].key)
        val array = createKeyValues.first().value_?.array_value?.values
        assertNotNull(array)
        assertEquals(array[0].string_value, "val")
        assertEquals(array[1].int_value, 32L)
    }

    @Test
    fun testUnknownTypeExpectException() {
        assertFailsWith(UnsupportedOperationException::class) {
            createForValue(Any())
        }
    }

    private fun createForValue(value: Any): KeyValue {
        return mapOf("key" to value).createKeyValues().first()
    }
}