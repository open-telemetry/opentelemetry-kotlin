package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.context.Context

/**
 * Injects and extracts cross-cutting concern values as string key/value pairs into
 * carriers that travel across process boundaries.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#textmap-propagator
 */
@ExperimentalApi
@ThreadSafe
public interface TextMapPropagator {

    /**
     * Returns the field names this propagator reads and writes.
     * Used by callers to extract relevant fields from carriers.
     */
    public fun fields(): Collection<String>

    /**
     * Injects propagated fields from [context] into [carrier] using [setter].
     */
    public fun <C> inject(context: Context, carrier: C, setter: TextMapSetter<C>)

    /**
     * Extracts propagated fields from [carrier] using [getter] and returns a new
     * [Context] with those values merged in.
     */
    public fun <C> extract(context: Context, carrier: C, getter: TextMapGetter<C>): Context
}
