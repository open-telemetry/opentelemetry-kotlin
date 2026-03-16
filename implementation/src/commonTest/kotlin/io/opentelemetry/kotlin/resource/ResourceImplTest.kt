package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test
    fun testMergeNonOverlapping() {
        val base = ResourceImpl(AttributesModel(attrs = mutableMapOf("a" to "1")), "https://example.com/base")
        val other = ResourceImpl(AttributesModel(attrs = mutableMapOf("b" to "2")), "https://example.com/other")
        val merged = base.merge(other)
        assertEquals("1", merged.attributes["a"])
        assertEquals("2", merged.attributes["b"])
    }

    @Test
    fun testMergeOtherWinsOnConflict() {
        val base = ResourceImpl(AttributesModel(attrs = mutableMapOf("key" to "base")), null)
        val other = ResourceImpl(AttributesModel(attrs = mutableMapOf("key" to "other")), null)
        val merged = base.merge(other)
        assertEquals("other", merged.attributes["key"])
    }

    @Test
    fun testMergeSchemaUrlBothNull() {
        val base = ResourceImpl(AttributesModel(), null)
        val other = ResourceImpl(AttributesModel(), null)
        assertNull(base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeSchemaUrlBaseNull() {
        val base = ResourceImpl(AttributesModel(), null)
        val other = ResourceImpl(AttributesModel(), "https://example.com/other")
        assertEquals("https://example.com/other", base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeSchemaUrlOtherNull() {
        val base = ResourceImpl(AttributesModel(), "https://example.com/base")
        val other = ResourceImpl(AttributesModel(), null)
        assertEquals("https://example.com/base", base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeSchemaUrlSame() {
        val base = ResourceImpl(AttributesModel(), "https://example.com/schema")
        val other = ResourceImpl(AttributesModel(), "https://example.com/schema")
        assertEquals("https://example.com/schema", base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeSchemaUrlDifferentOtherWins() {
        val base = ResourceImpl(AttributesModel(), "https://example.com/base")
        val other = ResourceImpl(AttributesModel(), "https://example.com/other")
        assertEquals("https://example.com/other", base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeAttributeLimit() {
        val baseAttrs = (0 until DEFAULT_ATTRIBUTE_LIMIT).associate { "base$it" to "v$it" }
        val otherAttrs = mapOf("extra" to "value")
        val base = ResourceImpl(AttributesModel(attrs = baseAttrs.toMutableMap()), null)
        val other = ResourceImpl(AttributesModel(attrs = otherAttrs.toMutableMap()), null)
        val merged = base.merge(other)
        assertEquals(DEFAULT_ATTRIBUTE_LIMIT, merged.attributes.size)
    }
}
