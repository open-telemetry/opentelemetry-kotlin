package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.safeExceptionAttributes
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
    val safeAttributes = safeExceptionAttributes(exception)

    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_STACKTRACE)) {
        safeAttributes.stacktrace?.let {
            setStringAttribute(ExceptionAttributes.EXCEPTION_STACKTRACE, it)
        }
    }
    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_MESSAGE)) {
        safeAttributes.message?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, it) }
    }
    if (container == null || !container.attributes.containsKey(ExceptionAttributes.EXCEPTION_TYPE)) {
        safeAttributes.type?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_TYPE, it) }
    }
}
