package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * DSL receiver for assembling [TraceFlags]. All flags default to false, per
 * https://www.w3.org/TR/trace-context/#trace-flags
 */
@TracingDsl
@ExperimentalApi
public interface TraceFlagsCreationAction {

    /**
     * True if the trace is sampled.
     */
    public var isSampled: Boolean

    /**
     * True if the trace is random.
     */
    public var isRandom: Boolean
}
