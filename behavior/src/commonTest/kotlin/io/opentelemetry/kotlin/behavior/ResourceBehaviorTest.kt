package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class ResourceBehaviorTest {

    @Test
    fun everyFieldStartsUnset() {
        val behavior = ResourceBehavior()
        assertNull(behavior.serviceName)
        assertNull(behavior.schemaUrl)
        assertNull(behavior.attributes)
    }

    @Test
    fun refinesRatherThanReplaces() {
        val merged = ResourceBehavior(
            serviceName = "checkout",
            schemaUrl = SCHEMA_URL,
            attributes = mapOf("a" to 1L, "b" to 2L),
        ).mergeWith(
            ResourceBehavior(schemaUrl = OTHER_SCHEMA_URL, attributes = mapOf("b" to 99L, "c" to 3L)),
        )
        assertEquals("checkout", merged.serviceName)
        assertEquals(OTHER_SCHEMA_URL, merged.schemaUrl)
        assertEquals(mapOf("a" to 1L, "b" to 99L, "c" to 3L), merged.attributes)
    }

    @Test
    fun adoptsEverythingWhenLowerIsUnset() {
        val higher = ResourceBehavior(
            serviceName = "checkout",
            schemaUrl = SCHEMA_URL,
            attributes = mapOf("a" to 1L),
        )
        assertEquals(higher, ResourceBehavior().mergeWith(higher))
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = ResourceBehavior(
            serviceName = "checkout",
            schemaUrl = SCHEMA_URL,
            attributes = mapOf("a" to 1L),
        )
        val higher = ResourceBehavior(
            serviceName = "payments",
            schemaUrl = OTHER_SCHEMA_URL,
            attributes = mapOf("a" to 9L),
        )
        assertEquals(higher, lower.mergeWith(higher))
    }

    @Test
    fun treatsEmptyValuesAsConfigured() {
        val merged = ResourceBehavior(serviceName = "checkout", attributes = mapOf("a" to 1L))
            .mergeWith(ResourceBehavior(serviceName = "", attributes = emptyMap()))
        assertEquals("", merged.serviceName)
        assertEquals(mapOf("a" to 1L), merged.attributes)
        assertNotEquals(ResourceBehavior(), ResourceBehavior(attributes = emptyMap()))
    }

    private companion object {
        const val SCHEMA_URL = "https://opentelemetry.io/schemas/1.37.0"
        const val OTHER_SCHEMA_URL = "https://opentelemetry.io/schemas/1.38.0"
    }
}
