package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.safeExceptionAttributes
import io.opentelemetry.kotlin.semconv.ExceptionAttributes

public fun AttributesMutator.setExceptionAttributes(exception: Throwable) {
    val attributes = safeExceptionAttributes(exception)
    setStringAttribute(ExceptionAttributes.EXCEPTION_STACKTRACE, attributes.stackTrace)
    attributes.message?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, it) }
    attributes.type?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_TYPE, it) }
}
