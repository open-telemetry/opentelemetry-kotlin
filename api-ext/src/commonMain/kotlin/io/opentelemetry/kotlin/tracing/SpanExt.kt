package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.exceptionType
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.Span

/**
 * Record an exception on the span as an event.
 */
@ExperimentalApi
@ThreadSafe
public fun Span.recordException(
    exception: Throwable,
    attributes: MutableAttributeContainer.() -> Unit = {}
) {
    addEvent("exception") {
        setStringAttribute("exception.stacktrace", exception.stackTraceToString())
        exception.message?.let {
            setStringAttribute("exception.message", it)
        }
        exception.exceptionType()?.let {
            setStringAttribute("exception.type", it)
        }
        attributes(this)
    }
}

/**
 * Adds a link to the span that associates it with another [Span].
 */
@ExperimentalApi
@ThreadSafe
public fun Span.addLink(span: Span, attributes: MutableAttributeContainer.() -> Unit = {}) {
    addLink(span.spanContext, attributes)
}

/**
 * Wraps the [action] with a span that automatically ends when the [action] completes. This
 * provides an alternative to manually ending spans. [action] must return the span
 * status - generally this will be [StatusData.Ok] unless the operation fails.
 *
 * If an exception is thrown it will be added to the span as an event and the status will be
 * set to [StatusData.Error] with a description of the throwable's message, if any.
 */
@ExperimentalApi
public fun Span.wrapOperation(action: () -> StatusData) {
    try {
        status = action()
    } catch (exc: Throwable) {
        recordException(exc)
        status = StatusData.Error(exc.message)
    } finally {
        end()
    }
}
