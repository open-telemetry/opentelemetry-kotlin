package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.exceptionType
import io.opentelemetry.kotlin.semconv.ExceptionAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class AttributesMutatorExtTest {

    @Test
    fun testSetExceptionAttributesSetsStacktrace() {
        val mutator = FakeAttributesMutator()
        val exception = IllegalArgumentException()

        mutator.setExceptionAttributes(exception)

        assertNotNull(mutator.attributes[ExceptionAttributes.EXCEPTION_STACKTRACE])
    }

    @Test
    fun testSetExceptionAttributesSetsExceptionType() {
        val mutator = FakeAttributesMutator()
        val exception = IllegalArgumentException()

        mutator.setExceptionAttributes(exception)

        assertEquals(exception.exceptionType(), mutator.attributes[ExceptionAttributes.EXCEPTION_TYPE])
    }

    @Test
    fun testSetExceptionAttributesSetsMessage() {
        val mutator = FakeAttributesMutator()
        val exception = IllegalStateException("something went wrong")

        mutator.setExceptionAttributes(exception)

        assertEquals("something went wrong", mutator.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
    }

    @Test
    fun testSetExceptionAttributesOmitsMessageWhenNull() {
        val mutator = FakeAttributesMutator()
        val exception = IllegalArgumentException()

        mutator.setExceptionAttributes(exception)

        assertNull(mutator.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
    }

    @Test
    fun testSetExceptionAttributesOmitsTypeForAnonymousException() {
        val mutator = FakeAttributesMutator()
        val exception = object : IllegalArgumentException() {}

        mutator.setExceptionAttributes(exception)

        assertNull(mutator.attributes[ExceptionAttributes.EXCEPTION_TYPE])
        assertNotNull(mutator.attributes[ExceptionAttributes.EXCEPTION_STACKTRACE])
    }

    @Test
    fun testSetExceptionAttributesDoesNotOverwriteExistingAttributes() {
        val mutator = FakeAttributesMutator()
        val exception = IllegalStateException("exception message")

        mutator.setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, "user message")
        mutator.setExceptionAttributes(exception)

        assertEquals("user message", mutator.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
        assertNotNull(mutator.attributes[ExceptionAttributes.EXCEPTION_STACKTRACE])
        assertNotNull(mutator.attributes[ExceptionAttributes.EXCEPTION_TYPE])
    }

    @Test
    fun testSetExceptionAttributesPreservesUserProvidedTypeAndStacktrace() {
        val mutator = FakeAttributesMutator()
        val exception = IllegalStateException("exception message")

        mutator.setStringAttribute(ExceptionAttributes.EXCEPTION_TYPE, "user.type")
        mutator.setStringAttribute(ExceptionAttributes.EXCEPTION_STACKTRACE, "user stack")
        mutator.setExceptionAttributes(exception)

        assertEquals("user.type", mutator.attributes[ExceptionAttributes.EXCEPTION_TYPE])
        assertEquals("user stack", mutator.attributes[ExceptionAttributes.EXCEPTION_STACKTRACE])
        assertEquals("exception message", mutator.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
    }

    @Test
    fun testSetExceptionAttributesKeepsUnrelatedAttributes() {
        val mutator = FakeAttributesMutator()
        val exception = IllegalStateException("exception message")

        mutator.setStringAttribute("user.key", "user value")
        mutator.setExceptionAttributes(exception)

        assertEquals("user value", mutator.attributes["user.key"])
        assertEquals("exception message", mutator.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
    }

    @Test
    fun testSetExceptionAttributesIsIdempotentForExistingValues() {
        val mutator = FakeAttributesMutator()
        val exception = IllegalStateException("exception message")

        mutator.setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, "user message")
        mutator.setExceptionAttributes(exception)
        mutator.setExceptionAttributes(IllegalArgumentException("other message"))

        assertEquals("user message", mutator.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
    }
}
