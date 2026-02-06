package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AtomicLongTest {

    @Test
    fun testDefaultInitialValue() {
        val atomic = AtomicLong()
        assertEquals(0L, atomic.get())
    }

    @Test
    fun testInitialValue() {
        val atomic = AtomicLong(42L)
        assertEquals(42L, atomic.get())
    }

    @Test
    fun testSet() {
        val atomic = AtomicLong(0L)
        atomic.set(100L)
        assertEquals(100L, atomic.get())
    }

    @Test
    fun testIncrementAndGet() {
        val atomic = AtomicLong(5L)
        assertEquals(6L, atomic.incrementAndGet())
        assertEquals(6L, atomic.get())
    }

    @Test
    fun testDecrementAndGet() {
        val atomic = AtomicLong(5L)
        assertEquals(4L, atomic.decrementAndGet())
        assertEquals(4L, atomic.get())
    }

    @Test
    fun testAddAndGet() {
        val atomic = AtomicLong(10L)
        assertEquals(15L, atomic.addAndGet(5L))
        assertEquals(15L, atomic.get())
    }

    @Test
    fun testAddAndGetNegativeDelta() {
        val atomic = AtomicLong(10L)
        assertEquals(7L, atomic.addAndGet(-3L))
        assertEquals(7L, atomic.get())
    }

    @Test
    fun testGetAndAdd() {
        val atomic = AtomicLong(10L)
        assertEquals(10L, atomic.getAndAdd(5L))
        assertEquals(15L, atomic.get())
    }

    @Test
    fun testCompareAndSetPass() {
        val atomic = AtomicLong(10L)
        assertTrue(atomic.compareAndSet(10L, 20L))
        assertEquals(20L, atomic.get())
    }

    @Test
    fun testCompareAndSetFail() {
        val atomic = AtomicLong(10L)
        assertFalse(atomic.compareAndSet(5L, 20L))
        assertEquals(10L, atomic.get())
    }

    @Test
    fun testLimits() {
        val atomic = AtomicLong(Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, atomic.get())

        atomic.set(Long.MIN_VALUE)
        assertEquals(Long.MIN_VALUE, atomic.get())
    }

    @Test
    fun testWrapAroundIncrement() {
        val atomic = AtomicLong(Long.MAX_VALUE)
        assertEquals(Long.MIN_VALUE, atomic.incrementAndGet())
    }

    @Test
    fun testWrapAroundDecrement() {
        val atomic = AtomicLong(Long.MIN_VALUE)
        assertEquals(Long.MAX_VALUE, atomic.decrementAndGet())
    }
}
