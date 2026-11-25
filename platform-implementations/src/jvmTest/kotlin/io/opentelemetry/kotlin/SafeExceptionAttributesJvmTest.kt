package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class SafeExceptionAttributesJvmTest {

    @Test
    fun toleratesHostileAccessorsWithoutCallingThrowableMethods() {
        val throwable = HostileThrowable()

        val attributes = safeExceptionAttributes(throwable)

        assertEquals(HostileThrowable::class.qualifiedName, attributes.type)
        assertEquals(HostileThrowable::class.java.name, attributes.message)
        val type = HostileThrowable::class.java.name
        assertEquals("$type: $type\n", attributes.stacktrace)
        assertEquals(1, throwable.messageReads)
    }

    @Test
    fun boundsMessageFramesCausesAndSuppressedExceptions() {
        var cause: Throwable = IllegalStateException("cause-4")
        repeat(4) { index -> cause = IllegalStateException("cause-$index", cause) }
        val throwable = IllegalStateException("m".repeat(2048), cause)
        throwable.stackTrace = Array(250) { StackTraceElement("Example", "frame$it", "Example.kt", it) }
        repeat(5) { throwable.addSuppressed(IllegalArgumentException("suppressed-$it")) }

        val attributes = safeExceptionAttributes(throwable)
        val stacktrace = assertNotNull(attributes.stacktrace)

        assertEquals(1024, assertNotNull(attributes.message).length)
        assertEquals(200, stacktrace.lineSequence().count { "\tat " in it })
        assertEquals(3, stacktrace.lineSequence().count { "Caused by: " in it })
        assertEquals(3, stacktrace.lineSequence().count { "Suppressed: " in it })
    }

    @Test
    fun detectsCauseCyclesByIdentity() {
        val throwable = CyclicThrowable()

        val stacktrace = assertNotNull(safeExceptionAttributes(throwable).stacktrace)

        assertTrue("[CIRCULAR REFERENCE: ${CyclicThrowable::class.java.name}]" in stacktrace)
    }

    @Test
    fun detectsSuppressedCyclesByIdentity() {
        val first = IllegalStateException("first")
        val second = IllegalStateException("second")
        first.addSuppressed(second)
        second.addSuppressed(first)

        val stacktrace = assertNotNull(safeExceptionAttributes(first).stacktrace)

        assertTrue("[CIRCULAR REFERENCE: ${IllegalStateException::class.java.name}]" in stacktrace)
    }

    @Suppress("ExceptionRaisedInUnexpectedLocation")
    private class HostileThrowable : Throwable() {
        var messageReads: Int = 0

        override val message: String
            get() {
                messageReads++
                throw AssertionError("message accessed")
            }

        override val cause: Throwable
            get() = throw AssertionError("cause accessed")

        override fun getStackTrace(): Array<StackTraceElement> =
            throw AssertionError("stack trace accessed")

        override fun toString(): String = throw AssertionError("toString called")
        override fun equals(other: Any?): Boolean = throw AssertionError("equals called")
        override fun hashCode(): Int = throw AssertionError("hashCode called")
    }

    private class CyclicThrowable : Throwable("cycle") {
        override val cause: Throwable
            get() = this
    }
}
