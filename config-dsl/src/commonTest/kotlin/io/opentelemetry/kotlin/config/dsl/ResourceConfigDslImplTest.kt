package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResourceConfigDslImplTest {

    @Test
    fun startsUnset() {
        assertEquals(ResourceBehavior(), ResourceConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsServiceNameSchemaAndAttributes() {
        val dsl = ResourceConfigDslImpl().apply {
            serviceName = "checkout"
            resource(schemaUrl = "https://example.com/schema") {
                setStringAttribute("service.namespace", "shop")
                setLongAttribute("service.instance.id", 42)
            }
        }

        assertEquals(
            ResourceBehavior(
                serviceName = "checkout",
                schemaUrl = "https://example.com/schema",
                attributes = mapOf(
                    "service.namespace" to "shop",
                    "service.instance.id" to 42L,
                ),
            ),
            dsl.toBehavior(),
        )
    }

    @Test
    fun mapValuesAreCopied() {
        val attributes = mutableMapOf<String, Any>("service.namespace" to "shop")
        val dsl = ResourceConfigDslImpl()
        dsl.resource(attributes)
        attributes["service.namespace"] = "changed"

        assertEquals(mapOf("service.namespace" to "shop"), dsl.toBehavior().attributes)
    }

    @Test
    fun mapValuesUseAttributeTypesAndDefensiveCopies() {
        val bytes = byteArrayOf(1, 2)
        val names = mutableListOf("a", "b")
        val dsl = ResourceConfigDslImpl()
        dsl.resource(mapOf("count" to 3, "bytes" to bytes, "names" to names))
        bytes[0] = 9
        names[0] = "changed"

        val attributes = dsl.toBehavior().attributes
        assertEquals(3L, attributes?.get("count"))
        assertEquals(listOf("a", "b"), attributes?.get("names"))
        assertEquals(listOf<Byte>(1, 2), (attributes?.get("bytes") as ByteArray).toList())
    }

    @Test
    fun anyValuesAreCopiedRecursively() {
        val bytes = byteArrayOf(1, 2)
        val list = mutableListOf<AnyValue>(AnyValue.BytesValue(bytes))
        val value = AnyValue.MapValue(mutableMapOf("nested" to AnyValue.ListValue(list)))
        val dsl = ResourceConfigDslImpl()
        dsl.resource(mapOf("any" to value))
        bytes[0] = 9
        list.clear()

        assertEquals(
            AnyValue.MapValue(
                mapOf("nested" to AnyValue.ListValue(listOf(AnyValue.BytesValue(byteArrayOf(1, 2)))))
            ),
            dsl.toBehavior().attributes?.get("any"),
        )
    }

    @Test
    fun serviceNameTakesPrecedenceOverAttribute() {
        val dsl = ResourceConfigDslImpl().apply {
            resource(mapOf("service.name" to "from-attributes"))
            serviceName = "from-property"
        }

        assertEquals("from-property", dsl.toBehavior().serviceName)
        assertEquals("from-attributes", dsl.toBehavior().attributes?.get("service.name"))
    }
}
