package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class ResourceImplTest {

    @Test
    fun testResourceImpl() {
        val attrs =
            AttributesModel(attrs = mutableMapOf("key" to "value"))
        val resource = ResourceImpl(attrs, "https://example.com/schema")
        val another = resource.asNewResource {
            attributes["foo"] = "value"
            schemaUrl = "https://example.com/another"
        }

        assertEquals("value", resource.attributes["key"])
        assertEquals("value", another.attributes["key"])

        assertNull(resource.attributes["foo"])
        assertEquals("value", another.attributes["foo"])

        assertEquals("https://example.com/schema", resource.schemaUrl)
        assertEquals("https://example.com/another", another.schemaUrl)
    }

    @Test
    fun testEmptyResource() {
        val attrs =
            AttributesModel(attrs = mutableMapOf("key" to "value"))
        val resource = ResourceImpl(attrs, "https://example.com/schema")
        val another = resource.asNewResource {
            attributes.clear()
            schemaUrl = null
        }
        assertEquals(emptyMap(), another.attributes)
        assertNull(another.schemaUrl)
    }

    @Test
    fun testDefensiveCopy() {
        val container = AttributesModel(attrs = mutableMapOf())
        val resource = ResourceImpl(container, null)
        lateinit var attrs: MutableMap<String, Any>
        val another = resource.asNewResource {
            attrs = attributes
        }
        attrs["key"] = "value"
        assertEquals(emptyMap(), another.attributes)
    }

    @Test
    fun testNewResourceAttributeLimit() {
        val attrs = (0..DEFAULT_ATTRIBUTE_LIMIT + 2).associate {
            "key$it" to "value$it"
        }
        val container = AttributesModel(attrs = attrs.toMutableMap())
        val resource = ResourceImpl(container, "https://example.com/schema")
        assertEquals(DEFAULT_ATTRIBUTE_LIMIT, resource.attributes.size)
    }

    @Test
    fun testMutateResourceAttributeLimit() {
        val attrs = (0..DEFAULT_ATTRIBUTE_LIMIT + 2).associate {
            "key$it" to "value$it"
        }
        val container = AttributesModel(attrs = attrs.toMutableMap())
        val resource = ResourceImpl(container, "https://example.com/schema")
        resource.asNewResource {
            attributes.putAll(attrs)
        }
        assertEquals(DEFAULT_ATTRIBUTE_LIMIT, resource.attributes.size)
    }
}
