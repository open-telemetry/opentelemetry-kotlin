package io.opentelemetry.kotlin.tracing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame

internal class SpanExtJvmTest {

    @Test
    fun wrapOperationToleratesHostileThrowable() {
        val span = FakeSpan("span")
        val throwable = HostileThrowable()

        val thrown = assertFailsWith<HostileThrowable> {
            span.wrapOperation { throw throwable }
        }

        assertSame(throwable, thrown)
        assertEquals(HostileThrowable::class.java.name, span.status.description)
        val attributes = span.events.single().attributes
        assertEquals(HostileThrowable::class.qualifiedName, attributes["exception.type"])
        assertEquals(HostileThrowable::class.java.name, attributes["exception.message"])
        assertNotNull(attributes["exception.stacktrace"])
    }

    @Suppress("ExceptionRaisedInUnexpectedLocation")
    private class HostileThrowable : Throwable() {
        override val message: String
            get() = throw AssertionError("message accessed")

        override fun getStackTrace(): Array<StackTraceElement> =
            throw AssertionError("stack trace accessed")

        override fun toString(): String = throw AssertionError("toString called")
    }
}
