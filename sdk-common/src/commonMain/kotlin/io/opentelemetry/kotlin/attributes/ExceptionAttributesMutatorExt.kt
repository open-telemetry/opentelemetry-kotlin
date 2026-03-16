package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.exceptionType
import io.opentelemetry.kotlin.semconv.ExceptionAttributes

/**
 * Sets exception attributes on this [AttributesMutator] from a [Throwable], following the
 * OpenTelemetry semantic conventions for exceptions.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/exceptions/
 */
@ExperimentalApi
public fun AttributesMutator.setExceptionAttributes(exception: Throwable) {
    setStringAttribute(ExceptionAttributes.EXCEPTION_STACKTRACE, exception.stackTraceToString())
    exception.message?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, it) }
    exception.exceptionType()?.let { setStringAttribute(ExceptionAttributes.EXCEPTION_TYPE, it) }
}
