package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.safeExceptionAttributes
import io.opentelemetry.kotlin.semconv.ExceptionAttributes

/**
 * Sets exception-derived attributes, only for keys that are not already present.
 * This ensures user-provided attributes take precedence as required by the
 * Logs SDK specification.
 */
public fun AttributesMutator.setExceptionAttributes(exception: Throwable) {
    val attributes = safeExceptionAttributes(exception)
    val container = this as? AttributeContainer

    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_STACKTRACE)) {
        setStringAttribute(ExceptionAttributes.EXCEPTION_STACKTRACE, attributes.stackTrace)
    }
    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_MESSAGE)) {
        attributes.message?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, it) }
    }
    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_TYPE)) {
        attributes.type?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_TYPE, it) }
    }
}
