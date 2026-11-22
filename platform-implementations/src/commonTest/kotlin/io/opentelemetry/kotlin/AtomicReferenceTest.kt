package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class AtomicReferenceTest {

    private data class Value(val name: String)

    @Test
    fun testInitialValue() {
        val initial = Value("a")
        val ref = AtomicReference(initial)
        assertSame(initial, ref.get())
    }

    @Test
    fun testNullableInitialValue() {
        val ref = AtomicReference<Value?>(null)
        assertNull(ref.get())
    }

    @Test
    fun testSet() {
        val ref = AtomicReference(Value("a"))
        val update = Value("b")
        ref.set(update)
        assertSame(update, ref.get())
    }

    @Test
    fun testCompareAndSetSuccess() {
        val initial = Value("a")
        val update = Value("b")
        val ref = AtomicReference(initial)
        assertTrue(ref.compareAndSet(initial, update))
        assertSame(update, ref.get())
    }

    @Test
    fun testCompareAndSetFailure() {
        val initial = Value("a")
        val ref = AtomicReference(initial)
        assertFalse(ref.compareAndSet(Value("other"), Value("b")))
        assertSame(initial, ref.get())
    }

    @Test
    fun testCompareAndSetFromNull() {
        val ref = AtomicReference<Value?>(null)
        val update = Value("a")
        assertTrue(ref.compareAndSet(null, update))
        assertSame(update, ref.get())
        assertFalse(ref.compareAndSet(null, Value("b")))
        assertSame(update, ref.get())
    }

    @Test
    fun testCompareAndSetUsesIdentityNotEquals() {
        val initial = Value("a")
        val equalButDistinct = Value("a")
        assertEquals(initial, equalButDistinct)
        assertNotSame(initial, equalButDistinct)

        val ref = AtomicReference(initial)
        assertFalse(ref.compareAndSet(equalButDistinct, Value("b")))
        assertSame(initial, ref.get())
    }
}
