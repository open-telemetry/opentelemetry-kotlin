package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.exceptionType
import io.opentelemetry.kotlin.semconv.ExceptionAttributes

/**
 * Sets exception-derived attributes, only for keys that are not already present.
 * This ensures user-provided attributes take precedence as required by the
 * Logs SDK specification.
 */
public fun AttributesMutator.setExceptionAttributes(exception: Throwable) {
    // If the receiver also implements AttributeContainer, check for existing keys
    // to avoid overwriting user-provided attributes.
    val container = this as? AttributeContainer

    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_STACKTRACE)) {
        setStringAttribute(ExceptionAttributes.EXCEPTION_STACKTRACE, exception.stackTraceToString())
    }
    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_MESSAGE)) {
        exception.message?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, it) }
    }
    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_TYPE)) {
        exception.exceptionType()?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_TYPE, it) }
    }
}
