package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ThreadLocalTest {

    @Test
    fun testReturnsNullWhenUnset() {
        val threadLocal = ThreadLocal<String>()
        assertNull(threadLocal.get())
    }

    @Test
    fun testRoundTrip() {
        val threadLocal = ThreadLocal<String>()
        threadLocal.set("value")
        assertEquals("value", threadLocal.get())
    }

    @Test
    fun testSetNullClears() {
        val threadLocal = ThreadLocal<String>()
        threadLocal.set("value")
        threadLocal.set(null)
        assertNull(threadLocal.get())
    }

    @Test
    fun testSeparateInstancesAreIndependent() {
        val first = ThreadLocal<String>()
        val second = ThreadLocal<String>()
        first.set("first")
        second.set("second")
        assertEquals("first", first.get())
        assertEquals("second", second.get())
    }
}
