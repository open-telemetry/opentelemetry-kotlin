package io.opentelemetry.kotlin.export

import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.KeyValue
import kotlin.test.assertEquals

fun assertAttributesMatch(
    attributes: Map<String, Any>,
    protobuf: List<KeyValue>,
) {
    assertEquals(attributes.size, protobuf.size)
    protobuf.forEach { attr ->
        val expected = attributes[attr.key]
        val observed = retrieveValue(attr.value_)
        assertEquals(expected, observed)
    }
}


private fun retrieveValue(obj: AnyValue?): Any? {
    return when {
        obj == null -> null
        obj.string_value != null -> obj.string_value
        obj.int_value != null -> obj.int_value
        obj.double_value != null -> obj.double_value
        obj.bool_value != null -> obj.bool_value
        obj.array_value != null -> obj.array_value.values.map {
            retrieveValue(it)
        }
        else -> error("Unknown type!")
    }
}
