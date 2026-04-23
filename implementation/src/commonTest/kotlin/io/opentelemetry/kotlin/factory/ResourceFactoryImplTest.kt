package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ResourceFactoryImplTest {

    private val factory = ResourceFactoryImpl()

    @Test
    fun testEmptyHasNoAttributes() {
        assertTrue(factory.empty.attributes.isEmpty())
    }

    @Test
    fun testEmptyHasNoSchemaUrl() {
        assertNull(factory.empty.schemaUrl)
    }

    @Test
    fun testEmptyReturnsSameInstance() {
        assertNotSame(factory.empty, ResourceFactoryImpl().empty)
    }

    @Test
    fun testCreateWithAttributes() {
        val resource = factory.create {
            setStringAttribute("key", "value")
        }
        assertEquals("value", resource.attributes["key"])
    }

    @Test
    fun testCreateWithSchemaUrl() {
        val resource = factory.create(schemaUrl = "https://example.com/schema") {}
        assertEquals("https://example.com/schema", resource.schemaUrl)
    }

    @Test
    fun testCreateDefaultSchemaUrlIsNull() {
        val resource = factory.create {}
        assertNull(resource.schemaUrl)
    }

    @Test
    fun testCreateWithAttributesAndSchemaUrl() {
        val resource = factory.create(schemaUrl = "https://example.com/schema") {
            setStringAttribute("service.name", "my-service")
        }
        assertEquals("my-service", resource.attributes["service.name"])
        assertEquals("https://example.com/schema", resource.schemaUrl)
    }

    @Test
    fun testCreateNoAttributeLimit() {
        val count = DEFAULT_ATTRIBUTE_LIMIT + 3
        val resource = factory.create {
            (0 until count).forEach { setStringAttribute("key$it", "value$it") }
        }
        assertEquals(count, resource.attributes.size)
    }
}
