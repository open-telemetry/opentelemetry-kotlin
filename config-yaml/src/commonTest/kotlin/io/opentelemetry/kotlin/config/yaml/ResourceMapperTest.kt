package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.config.schema.model.AttributeNameValue
import io.opentelemetry.kotlin.config.schema.model.AttributeType
import io.opentelemetry.kotlin.config.schema.model.Resource
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResourceMapperTest {

    @Test
    fun mapsEmptyResource() {
        assertEquals(ResourceBehavior(), Resource().toBehavior())
    }

    @Test
    fun mapsSchemaUrl() {
        assertEquals(
            ResourceBehavior(schemaUrl = "https://opentelemetry.io/schemas/1.37.0"),
            Resource(schemaUrl = "https://opentelemetry.io/schemas/1.37.0").toBehavior(),
        )
    }

    @Test
    fun mapsAttributesList() {
        val behavior = Resource(
            attributesList = listOf(
                "service.name=first",
                "encoded%2Ckey = value%3Done",
                "empty=",
                "service.name=checkout",
            ).joinToString(),
        ).toBehavior()

        assertEquals(
            mapOf(
                "service.name" to "checkout",
                "encoded,key" to "value=one",
                "empty" to "",
            ),
            behavior.attributes,
        )
    }

    @Test
    fun mapsTypedAttributes() {
        val behavior = Resource(
            attributes = listOf(
                attribute("string", "value"),
                attribute("bool", true, AttributeType.BOOL),
                attribute("int", 42, AttributeType.INT),
                attribute("double", 4.2, AttributeType.DOUBLE),
                attribute("strings", listOf("one", "two"), AttributeType.STRING_ARRAY),
                attribute("bools", listOf(true, false), AttributeType.BOOL_ARRAY),
                attribute("ints", listOf(1, 2), AttributeType.INT_ARRAY),
                attribute("doubles", listOf(1.2, 3.4), AttributeType.DOUBLE_ARRAY),
            ),
        ).toBehavior()

        assertEquals(
            mapOf(
                "string" to "value",
                "bool" to true,
                "int" to 42L,
                "double" to 4.2,
                "strings" to listOf("one", "two"),
                "bools" to listOf(true, false),
                "ints" to listOf(1L, 2L),
                "doubles" to listOf(1.2, 3.4),
            ),
            behavior.attributes,
        )
    }

    @Test
    fun typedAttributesTakePrecedenceOverAttributesList() {
        val behavior = Resource(
            attributesList = "service.name=from-list,service.version=1.0",
            attributes = listOf(attribute("service.name", "from-attributes")),
        ).toBehavior()

        assertEquals(
            mapOf(
                "service.name" to "from-attributes",
                "service.version" to "1.0",
            ),
            behavior.attributes,
        )
    }

    @Test
    fun ignoresInvalidAttributesList() {
        listOf(
            "valid=value,invalid",
            "valid=value,invalid=%",
        ).forEach { attributesList ->
            val behavior = Resource(
                attributesList = attributesList,
                attributes = listOf(attribute("direct", "value")),
            ).toBehavior()

            assertEquals(mapOf("direct" to "value"), behavior.attributes)
        }
    }

    @Test
    fun ignoresInvalidTypedAttributes() {
        val behavior = Resource(
            attributes = listOf(
                attribute("", "empty-name"),
                attribute("null-value", null),
                attribute("wrong-string-type", 123, AttributeType.STRING),
                attribute("wrong-type", "true", AttributeType.BOOL),
                attribute("wrong-int-type", "42", AttributeType.INT),
                attribute("fractional-int", 1.5, AttributeType.INT),
                attribute("non-finite-int", Double.NaN, AttributeType.INT),
                attribute("wrong-double-type", "4.2", AttributeType.DOUBLE),
                attribute("non-list-array", "one", AttributeType.STRING_ARRAY),
                attribute("mixed-array", listOf("one", 2), AttributeType.STRING_ARRAY),
                attribute("fractional-ints", listOf(1, 2.5), AttributeType.INT_ARRAY),
                attribute("mixed-ints", listOf(1, "2"), AttributeType.INT_ARRAY),
                attribute("mixed-doubles", listOf(1.2, "3.4"), AttributeType.DOUBLE_ARRAY),
                attribute("valid", true, AttributeType.BOOL),
            ),
        ).toBehavior()

        assertEquals(mapOf("valid" to true), behavior.attributes)
    }

    private fun attribute(
        name: String,
        value: Any?,
        type: AttributeType? = null,
    ): AttributeNameValue = AttributeNameValue(name = name, value = value, type = type)
}
