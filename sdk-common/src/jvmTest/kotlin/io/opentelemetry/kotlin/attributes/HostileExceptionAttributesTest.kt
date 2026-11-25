package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.semconv.ExceptionAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class HostileExceptionAttributesTest {

    @Test
    fun setExceptionAttributesToleratesHostileThrowable() {
        val mutator = FakeAttributesMutator()

        mutator.setExceptionAttributes(HostileThrowable())

        assertEquals(HostileThrowable::class.qualifiedName, mutator.attributes[ExceptionAttributes.EXCEPTION_TYPE])
        assertEquals(HostileThrowable::class.java.name, mutator.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
        assertNotNull(mutator.attributes[ExceptionAttributes.EXCEPTION_STACKTRACE])
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
