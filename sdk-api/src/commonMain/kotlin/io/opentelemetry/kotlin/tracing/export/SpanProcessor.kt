package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

/**
 * Processes spans before they are exported as batches.
 */
@ExperimentalApi
public interface SpanProcessor : TelemetryCloseable {

    /**
     * Invoked when a span is created.
     *
     * @param span A reference to the span that has been created. This reference can be held & used to update the span.
     * @param parentContext The context of the parent span or the current context if there is none.
     */
    public fun onStart(span: ReadWriteSpan, parentContext: Context)

    /**
     * Invoked just before a span is about to end with a mutable representation of the span.
     */
    public fun onEnding(span: ReadWriteSpan)

    /**
     * Invoked after a span has ended with an immutable representation of the span.
     */
    public fun onEnd(span: ReadableSpan)

    /**
     * Determines whether [onStart] should be invoked when a span starts. Defaults to `true`; override
     * and return `false` if [onStart] does nothing, so the SDK can skip the call.
     */
    public fun isStartRequired(): Boolean = true

    /**
     * Determines whether [onEnd] should be invoked when a span ends. Defaults to `true`; override
     * and return `false` if [onEnd] does nothing, so the SDK can skip the call.
     */
    public fun isEndRequired(): Boolean = true

    /**
     * Determines whether [onEnding] should be invoked when a span is ending. Defaults to `true`;
     * override and return `false` if [onEnding] does nothing, so the SDK can skip the call.
     */
    public fun isOnEndingRequired(): Boolean = true
}
