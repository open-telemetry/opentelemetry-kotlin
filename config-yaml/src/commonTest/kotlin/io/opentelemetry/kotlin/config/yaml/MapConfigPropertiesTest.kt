package io.opentelemetry.kotlin.config.yaml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class MapConfigPropertiesTest {

    @Test
    fun absentPropertyReturnsNull() {
        val properties = MapConfigProperties(emptyMap())
        assertAllGettersNull(properties, "missing")
        assertTrue(properties.propertyKeys.isEmpty())
    }

    @Test
    fun nullPropertyReturnsNullButIsListedInKeys() {
        val properties = MapConfigProperties(mapOf("key" to null))
        assertAllGettersNull(properties, "key")
        assertEquals(setOf("key"), properties.propertyKeys)
    }

    @Test
    fun scalarsOfTheRequestedTypeAreReturned() {
        val properties = MapConfigProperties(
            mapOf("s" to "value", "b" to true, "i" to 42, "l" to 42L, "d" to 42.5)
        )
        assertEquals("value", properties.getString("s"))
        assertEquals(true, properties.getBoolean("b"))
        assertEquals(42L, properties.getLong("i"))
        assertEquals(42L, properties.getLong("l"))
        assertEquals(42.5, properties.getDouble("d"))
    }

    @Test
    fun numbersCanBeReadAsWiderTypes() {
        val properties = MapConfigProperties(mapOf("i" to 42, "w" to 42.0, "b" to true))
        assertEquals(42.0, properties.getDouble("i"))
        assertEquals(42L, properties.getLong("w"))
        assertEquals("42", properties.getString("i"))
        assertEquals("true", properties.getString("b"))
    }

    @Test
    fun scalarsOfAnotherTypeAreNotConverted() {
        val properties = MapConfigProperties(mapOf("s" to "42", "b" to "true", "i" to 42, "d" to 42.9))
        assertNull(properties.getLong("s"))
        assertNull(properties.getDouble("s"))
        assertNull(properties.getBoolean("b"))
        assertNull(properties.getBoolean("i"))
        assertNull(properties.getLong("d"))
    }

    @Test
    fun mappingsAndSequencesAreNotScalars() {
        val properties = MapConfigProperties(mapOf("m" to mapOf("a" to 1), "l" to listOf(1)))
        assertNull(properties.getString("m"))
        assertNull(properties.getLong("l"))
        assertNull(properties.getStructured("l"))
        assertNull(properties.getStringList("m"))
    }

    @Test
    fun sequencesOfTheRequestedTypeAreReturned() {
        val properties = MapConfigProperties(
            mapOf(
                "s" to listOf("a", "b"),
                "b" to listOf(true, false),
                "l" to listOf(1, 2L),
                "d" to listOf(1, 2.5),
            )
        )
        assertEquals(listOf("a", "b"), properties.getStringList("s"))
        assertEquals(listOf(true, false), properties.getBooleanList("b"))
        assertEquals(listOf(1L, 2L), properties.getLongList("l"))
        assertEquals(listOf(1.0, 2.5), properties.getDoubleList("d"))
    }

    @Test
    fun sequenceWithAnyMistypedElementReturnsNull() {
        val properties = MapConfigProperties(mapOf("list" to listOf(1, "two", 3), "nested" to listOf("a", listOf("b"))))
        assertNull(properties.getLongList("list"))
        assertNull(properties.getStringList("nested"))
    }

    @Test
    fun nestedMappingsAreStructured() {
        val properties = MapConfigProperties(
            mapOf("a" to mapOf("b" to mapOf("key" to "value")))
        )
        assertEquals("value", properties.getStructured("a")?.getStructured("b")?.getString("key"))
    }

    @Test
    fun sequenceOfMappingsIsStructured() {
        val properties = MapConfigProperties(
            mapOf("list" to listOf(mapOf("a" to 1), mapOf("a" to 2)), "mixed" to listOf(mapOf("a" to 1), "x"))
        )
        assertEquals(listOf(1L, 2L), properties.getStructuredList("list")?.map { it.getLong("a") })
        assertNull(properties.getStructuredList("mixed"))
    }

    @Test
    fun nonStringMappingKeysAreIgnored() {
        val nested: Map<*, *> = mapOf(1 to "one", "key" to "value")
        val child = MapConfigProperties(mapOf("child" to nested)).getStructured("child")
        assertEquals(setOf("key"), child?.propertyKeys)
    }

    @Test
    fun absentNodeConvertsToEmptyProperties() {
        val node: Map<String, Any?>? = null
        assertTrue(node.toConfigProperties().propertyKeys.isEmpty())
        assertEquals("value", mapOf("key" to "value").toConfigProperties().getString("key"))
    }

    private fun assertAllGettersNull(properties: MapConfigProperties, name: String) {
        assertNull(properties.getString(name))
        assertNull(properties.getBoolean(name))
        assertNull(properties.getLong(name))
        assertNull(properties.getDouble(name))
        assertNull(properties.getStringList(name))
        assertNull(properties.getBooleanList(name))
        assertNull(properties.getLongList(name))
        assertNull(properties.getDoubleList(name))
        assertNull(properties.getStructured(name))
        assertNull(properties.getStructuredList(name))
    }
}
