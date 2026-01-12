package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ThreadSafeMapTest {

    @Test
    fun testPutAndGet() {
        val map = threadSafeMap<String, Int>()
        map["a"] = 1
        map["b"] = 2
        assertEquals(1, map["a"])
        assertEquals(2, map["b"])
        assertEquals(2, map.size)
    }

    @Test
    fun testRemove() {
        val map = threadSafeMap<String, Int>()
        map["x"] = 42
        assertEquals(42, map.remove("x"))
        assertNull(map["x"])
    }

    @Test
    fun testClear() {
        val map = threadSafeMap<String, Int>()
        map["a"] = 1
        map["b"] = 2
        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun testContains() {
        val map = threadSafeMap<String, String>()
        map["foo"] = "bar"
        assertTrue(map.containsKey("foo"))
        assertTrue(map.containsValue("bar"))
        assertFalse(map.containsKey("baz"))
    }
}
