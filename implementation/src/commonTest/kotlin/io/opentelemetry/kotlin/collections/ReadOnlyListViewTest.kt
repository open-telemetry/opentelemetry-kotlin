package io.opentelemetry.kotlin.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ReadOnlyListViewTest {

    @Test
    fun reflectsDelegateContents() {
        val delegate = mutableListOf("a", "b")
        val view = ReadOnlyListView(delegate)

        assertEquals(listOf("a", "b"), view)
    }

    @Test
    fun reflectsLiveUpdatesToDelegate() {
        val delegate = mutableListOf("a")
        val view = ReadOnlyListView(delegate)

        delegate.add("b")

        assertEquals(listOf("a", "b"), view)
    }

    @Test
    fun isNotAMutableList() {
        val delegate = mutableListOf("a")
        val view = ReadOnlyListView(delegate)

        assertFailsWith<ClassCastException> {
            @Suppress("UNCHECKED_CAST")
            view as MutableList<String>
        }
    }
}
