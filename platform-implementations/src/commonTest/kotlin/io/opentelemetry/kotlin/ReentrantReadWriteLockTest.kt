package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ReentrantReadWriteLockTest {

    @Test
    fun testAddAndGet() {
        val list = threadSafeList<Int>()
        list.add(1)
        list.add(2)
        assertEquals(2, list.size)
        assertEquals(1, list[0])
        assertEquals(2, list[1])
    }

    @Test
    fun testRemove() {
        val list = threadSafeList<Int>().apply { addAll(listOf(1, 2, 3)) }
        assertTrue(list.remove(2))
        assertEquals(listOf(1, 3), list.toList())
    }

    @Test
    fun testClear() {
        val list = threadSafeList<Int>().apply { addAll(listOf(1, 2)) }
        list.clear()
        assertTrue(list.isEmpty())
    }

    @Test
    fun testContains() {
        val list = threadSafeList<String>().apply { addAll(listOf("a", "b")) }
        assertTrue(list.contains("a"))
        assertFalse(list.contains("c"))
    }
}
