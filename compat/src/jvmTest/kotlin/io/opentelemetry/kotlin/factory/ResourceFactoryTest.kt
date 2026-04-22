package io.opentelemetry.kotlin.factory

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ResourceFactoryTest {

    private val factory = CompatResourceFactory

    @Test
    fun `empty has no attributes`() {
        assertTrue(factory.empty.attributes.isEmpty())
    }

    @Test
    fun `empty has no schema url`() {
        assertNull(factory.empty.schemaUrl)
    }

    @Test
    fun `create with schema url`() {
        val resource = factory.create(schemaUrl = "https://example.com/schema") {}
        assertEquals("https://example.com/schema", resource.schemaUrl)
    }

    @Test
    fun `create default schema url is null`() {
        val resource = factory.create {}
        assertNull(resource.schemaUrl)
    }

    @Test
    fun `create with attributes`() {
        val resource = factory.create {
            setStringAttribute("key", "value")
        }
        assertEquals("value", resource.attributes["key"])
    }

    @Test
    fun `create with schema url and attributes`() {
        val resource = factory.create(schemaUrl = "https://example.com/schema") {
            setStringAttribute("service.name", "my-service")
        }
        assertEquals("my-service", resource.attributes["service.name"])
        assertEquals("https://example.com/schema", resource.schemaUrl)
    }
}
