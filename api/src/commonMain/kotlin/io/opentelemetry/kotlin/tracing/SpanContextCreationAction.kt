package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Configures the optional properties of a [SpanContext] created via [createSpanContext].
 */
@ExperimentalApi
@TracingDsl
public interface SpanContextCreationAction {

    /**
     * Whether the trace is sampled. Defaults to false.
     */
    public var isSampled: Boolean

    /**
     * Whether the trace ID is random. Defaults to false.
     */
    public var isRandom: Boolean

    /**
     * Whether the SpanContext was propagated from a remote parent. Defaults to false.
     */
    public var isRemote: Boolean

    /**
     * Configures the [TraceState] entries inside the [action] DSL block. Omitting this
     * results in an empty [TraceState].
     */
    public fun traceState(action: TraceStateCreationAction.() -> Unit)
}
