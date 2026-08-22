package io.opentelemetry.kotlin.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ReadOnlyMapViewTest {

    @Test
    fun reflectsDelegateContents() {
        val delegate = mutableMapOf("key" to "value")
        val view = ReadOnlyMapView(delegate)

        assertEquals(mapOf("key" to "value"), view)
    }

    @Test
    fun reflectsLiveUpdatesToDelegate() {
        val delegate = mutableMapOf("key" to "value")
        val view = ReadOnlyMapView(delegate)

        delegate["key2"] = "value2"

        assertEquals(mapOf("key" to "value", "key2" to "value2"), view)
    }

    @Test
    fun isNotAMutableMap() {
        val delegate = mutableMapOf("key" to "value")
        val view = ReadOnlyMapView(delegate)

        assertFailsWith<ClassCastException> {
            @Suppress("UNCHECKED_CAST")
            view as MutableMap<String, String>
        }
    }
}
