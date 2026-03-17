package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.exceptionType
import io.opentelemetry.kotlin.semconv.ExceptionAttributes

public fun AttributesMutator.setExceptionAttributes(exception: Throwable) {
    setStringAttribute(ExceptionAttributes.EXCEPTION_STACKTRACE, exception.stackTraceToString())
    exception.message?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, it) }
    exception.exceptionType()?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_TYPE, it) }
}
