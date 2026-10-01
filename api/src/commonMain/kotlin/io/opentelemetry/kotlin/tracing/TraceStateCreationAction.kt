package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * DSL receiver for assembling a [TraceState].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#tracestate
 */
@TracingDsl
@ExperimentalApi
public interface TraceStateCreationAction {

    /**
     * Adds an entry to the trace state. If [key] already has an entry, its value is replaced.
     *
     * Entries that fail W3C key/value validation (see [TraceState.put]), or that would exceed
     * the maximum number of entries, are ignored.
     */
    public fun put(key: String, value: String)
}
