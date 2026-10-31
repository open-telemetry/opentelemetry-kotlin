package io.opentelemetry.kotlin.resource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class EntityImplTest {

    @Test
    fun testEntityImpl() {
        val entity = EntityImpl(
            type = "service",
            schemaUrl = "https://example.com/schema",
            id = mapOf("service.name" to "my-service"),
            description = mapOf("org" to "open-telemetry"),
        )
        assertEquals("service", entity.type)
        assertEquals("https://example.com/schema", entity.schemaUrl)
        assertEquals("my-service", entity.id["service.name"])
        assertEquals("open-telemetry", entity.description["org"])
    }

    @Test
    fun testEqualsAndHashCodeByTypeSchemaUrlIdAndDescription() {
        val a = EntityImpl(
            type = "service",
            schemaUrl = "https://example.com/schema",
            id = mapOf("service.name" to "my-service"),
            description = mapOf("org" to "open-telemetry"),
        )
        val b = EntityImpl(
            type = "service",
            schemaUrl = "https://example.com/schema",
            id = mapOf("service.name" to "my-service"),
            description = mapOf("org" to "open-telemetry"),
        )
        val differentType = EntityImpl(
            type = "different",
            schemaUrl = "https://example.com/schema",
            id = mapOf("service.name" to "my-service"),
            description = mapOf("org" to "open-telemetry"),
        )
        val differentSchemaUrl = EntityImpl(
            type = "service",
            schemaUrl = "https://example.com/other",
            id = mapOf("service.name" to "my-service"),
            description = mapOf("org" to "open-telemetry"),
        )
        val differentId = EntityImpl(
            type = "service",
            schemaUrl = "https://example.com/schema",
            id = mapOf("service.name" to "other-service"),
            description = mapOf("org" to "open-telemetry"),
        )
        val differentDescription = EntityImpl(
            type = "service",
            schemaUrl = "https://example.com/schema",
            id = mapOf("service.name" to "my-service"),
            description = mapOf("org" to "other"),
        )

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, differentType)
        assertNotEquals(a, differentSchemaUrl)
        assertNotEquals(a, differentId)
        assertNotEquals(a, differentDescription)
    }
}
