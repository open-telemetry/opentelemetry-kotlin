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
                get() = error("hostile message")

            override fun getStackTrace(): Array<StackTraceElement> =
                error("hostile stack trace")

            override fun toString(): String = error("hostile toString")

            override fun equals(other: Any?): Boolean = error("hostile equals")

            override fun hashCode(): Int = error("hostile hashCode")
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
        assertEquals(3, attributes.stackTrace.lines().count { it.startsWith("Caused by: ") })
        assertContains(attributes.stackTrace, "suppressed exceptions omitted")
        assertContains(attributes.stackTrace, "frames omitted")
    }

    @Test
    fun truncatesMessagesByUtf8BytesWithoutSplittingCharacters() {
        val attributes = safeExceptionAttributes(RuntimeException("€".repeat(1024)))

        assertEquals(341, attributes.message?.length)
        assertEquals("😀".repeat(256), truncateUtf8("😀".repeat(257)))
    }

    @Test
    fun skipsStackTraceReadsAfterFrameBudgetIsExhausted() {
        val cause = CountingStackTraceThrowable()
        val root = RuntimeException("root", cause)
        root.stackTrace = Array(200) { index -> StackTraceElement("Example", "method$index", "Example.kt", index) }

        safeExceptionAttributes(root)

        assertEquals(0, cause.stackTraceReads)
    }

    private class CountingStackTraceThrowable : Throwable("cause") {
        var stackTraceReads: Int = 0

        override fun getStackTrace(): Array<StackTraceElement> {
            stackTraceReads++
            return emptyArray()
        }
    }
}
