package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ResourceFactoryImplTest {

    private val errors = FakeSdkErrorHandler()
    private val factory = ResourceFactoryImpl(errors)

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
        assertNotSame(factory.empty, ResourceFactoryImpl(errors).empty)
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

    @Test
    fun testCreateEntityWithId() {
        val entity = factory.createEntity("service") {
            id { setStringAttribute("service.name", "my-service") }
        }

        assertNotNull(entity)
        assertEquals("service", entity.type)
        assertNull(entity.schemaUrl)
        assertEquals(mapOf("service.name" to "my-service"), entity.id)
        assertEquals(emptyMap(), entity.description)
        assertFalse(errors.hasErrors())
    }

    @Test
    fun testCreateEntityWithSchemaUrlAndDescription() {
        val entity = factory.createEntity("service", "https://example.com/schema") {
            id { setStringAttribute("service.name", "my-service") }
            description { setStringAttribute("org", "open-telemetry") }
        }

        assertNotNull(entity)
        assertEquals("https://example.com/schema", entity.schemaUrl)
        assertEquals(mapOf("org" to "open-telemetry"), entity.description)
        assertFalse(errors.hasErrors())
    }

    @Test
    fun testCreateEntityEmptyTypeReturnsNull() {
        val entity = factory.createEntity("") {
            id { setStringAttribute("service.name", "my-service") }
        }

        assertNull(entity)
        assertEquals(1, errors.apiMisuses.size)
    }

    @Test
    fun testCreateEntityWithoutIdReturnsNull() {
        val entity = factory.createEntity("service") {
            description { setStringAttribute("org", "open-telemetry") }
        }

        assertNull(entity)
        assertEquals(1, errors.apiMisuses.size)
    }

    @Test
    fun testCreateEntityWithOnlyEmptyIdKeyReturnsNull() {
        val entity = factory.createEntity("service") {
            id { setStringAttribute("", "my-service") }
        }

        assertNull(entity)
        assertEquals(1, errors.apiMisuses.size)
    }

    @Test
    fun testCreateEntityUserCodeThrowsReturnsNull() {
        val entity = factory.createEntity("service") {
            id { setStringAttribute("service.name", "my-service") }
            error("error")
        }

        assertNull(entity)
        assertEquals(1, errors.userCodeErrors.size)
    }

    @Test
    fun testCreateEntityNoAttributeLimit() {
        val count = DEFAULT_ATTRIBUTE_LIMIT + 3
        val entity = factory.createEntity("service") {
            id { (0 until count).forEach { setStringAttribute("key$it", "value$it") } }
        }

        assertNotNull(entity)
        assertEquals(count, entity.id.size)
    }
}
