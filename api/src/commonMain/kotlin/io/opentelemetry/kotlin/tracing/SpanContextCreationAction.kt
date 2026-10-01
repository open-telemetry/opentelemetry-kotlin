package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * DSL receiver for assembling a [SpanContext] via [createSpanContext].
 */
@TracingDsl
@ExperimentalApi
public interface SpanContextCreationAction {

    /**
     * Configures the [TraceFlags]. If called more than once, the last call wins.
     */
    public fun traceFlags(action: TraceFlagsCreationAction.() -> Unit)

    /**
     * Configures the [TraceState]. If called more than once, the last call wins.
     */
    public fun traceState(action: TraceStateCreationAction.() -> Unit)
}
