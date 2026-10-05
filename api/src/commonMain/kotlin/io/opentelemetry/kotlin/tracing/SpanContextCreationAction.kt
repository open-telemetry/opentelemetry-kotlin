package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Configures the optional properties of a [SpanContext] created via [createSpanContext].
 */
@OptIn(ExperimentalApi::class)
@TracingDsl
internal interface SpanContextCreationAction {

    /**
     * Whether the trace is sampled. Defaults to false.
     */
    var isSampled: Boolean

    /**
     * Whether the trace ID is random. Defaults to false.
     */
    var isRandom: Boolean

    /**
     * Whether the SpanContext was propagated from a remote parent. Defaults to false.
     */
    var isRemote: Boolean

    /**
     * Configures the [TraceState] entries inside the [action] DSL block. Omitting this
     * results in an empty [TraceState].
     */
    fun traceState(action: TraceStateCreationAction.() -> Unit)
}
