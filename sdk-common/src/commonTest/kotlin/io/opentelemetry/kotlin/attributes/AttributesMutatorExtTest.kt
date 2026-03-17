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
}
