package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AtomicBooleanTest {

    @Test
    fun testDefaultInitialValue() {
        val atomic = AtomicBoolean()
        assertFalse(atomic.get())
    }

    @Test
    fun testInitialValue() {
        val atomic = AtomicBoolean(true)
        assertTrue(atomic.get())
    }

    @Test
    fun testSet() {
        val atomic = AtomicBoolean(false)
        atomic.set(true)
        assertTrue(atomic.get())

        atomic.set(false)
        assertFalse(atomic.get())
    }

    @Test
    fun testCompareAndSetPass() {
        val atomic = AtomicBoolean(false)
        assertTrue(atomic.compareAndSet(false, true))
        assertTrue(atomic.get())
    }

    @Test
    fun testCompareAndSetFail() {
        val atomic = AtomicBoolean(false)
        assertFalse(atomic.compareAndSet(true, false))
        assertFalse(atomic.get())
    }

    @Test
    fun testCompareAndSetOnlySucceedsOnce() {
        val atomic = AtomicBoolean(false)
        assertTrue(atomic.compareAndSet(false, true))
        assertFalse(atomic.compareAndSet(false, true))
        assertTrue(atomic.get())
    }

    @Test
    fun testCompareAndSetToSameValue() {
        val atomic = AtomicBoolean(true)
        assertTrue(atomic.compareAndSet(true, true))
        assertTrue(atomic.get())
    }

    @Test
    fun testInstancesAreIndependent() {
        val first = AtomicBoolean(false)
        val second = AtomicBoolean(false)
        first.set(true)

        assertEquals(true, first.get())
        assertEquals(false, second.get())
    }
}
