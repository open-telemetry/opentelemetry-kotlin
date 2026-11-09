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

    @Test
    fun testSetExceptionAttributesFallsBackForNonContainerMutator() {
        val mutator = MutatorOnlyAttributesMutator()
        val exception = IllegalStateException("exception message")

        mutator.setExceptionAttributes(exception)

        assertNotNull(mutator.values[ExceptionAttributes.EXCEPTION_STACKTRACE])
        assertEquals("exception message", mutator.values[ExceptionAttributes.EXCEPTION_MESSAGE])
        assertEquals(exception.exceptionType(), mutator.values[ExceptionAttributes.EXCEPTION_TYPE])
    }

    @Test
    fun testSetExceptionAttributesNonContainerMutatorWithNullMessage() {
        val mutator = MutatorOnlyAttributesMutator()
        val exception = IllegalArgumentException()

        mutator.setExceptionAttributes(exception)

        assertNotNull(mutator.values[ExceptionAttributes.EXCEPTION_STACKTRACE])
        assertEquals(exception.exceptionType(), mutator.values[ExceptionAttributes.EXCEPTION_TYPE])
        assertNull(mutator.values[ExceptionAttributes.EXCEPTION_MESSAGE])
    }

    /** An [AttributesMutator] that deliberately does not implement [AttributeContainer]. */
    private class MutatorOnlyAttributesMutator : AttributesMutator {
        val values = mutableMapOf<String, Any>()

        override fun setBooleanAttribute(key: String, value: Boolean) { values[key] = value }
        override fun setStringAttribute(key: String, value: String) { values[key] = value }
        override fun setLongAttribute(key: String, value: Long) { values[key] = value }
        override fun setDoubleAttribute(key: String, value: Double) { values[key] = value }
        override fun setBooleanListAttribute(key: String, value: List<Boolean>) { values[key] = value }
        override fun setStringListAttribute(key: String, value: List<String>) { values[key] = value }
        override fun setLongListAttribute(key: String, value: List<Long>) { values[key] = value }
        override fun setDoubleListAttribute(key: String, value: List<Double>) { values[key] = value }
        override fun setByteArrayAttribute(key: String, value: ByteArray) { values[key] = value }
        override fun setAnyValueAttribute(key: String, value: AnyValue) { values[key] = value }
    }
}
