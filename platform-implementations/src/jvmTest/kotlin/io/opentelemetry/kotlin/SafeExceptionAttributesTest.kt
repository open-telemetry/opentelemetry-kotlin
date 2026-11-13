package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SafeExceptionAttributesTest {

    @Test
    fun toleratesThrowingThrowableAccessors() {
        val exception = object : RuntimeException("unused") {
            override val message: String?
                get() = throw IllegalStateException("hostile message")

            override fun getStackTrace(): Array<StackTraceElement> =
                throw IllegalStateException("hostile stack trace")

            override fun toString(): String = throw IllegalStateException("hostile toString")

            override fun equals(other: Any?): Boolean = throw IllegalStateException("hostile equals")

            override fun hashCode(): Int = throw IllegalStateException("hostile hashCode")
        }

        val attributes = safeExceptionAttributes(exception)

        assertEquals(exception.javaClass.name, attributes.message)
        assertContains(attributes.stackTrace, exception.javaClass.name)
    }

    @Test
    fun detectsCauseAndSuppressedCyclesByIdentity() {
        val root = RuntimeException("root")
        val cause = RuntimeException("cause")
        root.initCause(cause)
        cause.initCause(root)
        root.addSuppressed(cause)
        cause.addSuppressed(root)

        val attributes = safeExceptionAttributes(root)

        assertContains(attributes.stackTrace, "[CIRCULAR REFERENCE:")
    }

    @Test
    fun limitsMessagesCausesSuppressedExceptionsAndFrames() {
        val root = RuntimeException("m".repeat(2048))
        root.stackTrace = Array(205) { index -> StackTraceElement("Example", "method$index", "Example.kt", index) }
        val causes = List(5) { RuntimeException("cause-$it") }
        root.initCause(causes.first())
        causes.zipWithNext().forEach { (current, next) -> current.initCause(next) }
        repeat(5) { root.addSuppressed(RuntimeException("suppressed-$it")) }

        val attributes = safeExceptionAttributes(root)

        assertEquals(1024, attributes.message?.length)
        assertTrue(attributes.stackTrace.lines().count { it.trimStart().startsWith("at ") } <= 200)
        assertContains(attributes.stackTrace, "causes omitted")
        assertContains(attributes.stackTrace, "suppressed exceptions omitted")
        assertContains(attributes.stackTrace, "frames omitted")
    }
}