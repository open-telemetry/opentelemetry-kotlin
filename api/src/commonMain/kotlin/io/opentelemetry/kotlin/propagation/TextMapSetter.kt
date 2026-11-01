package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Writes propagated fields into a carrier.
 *
 * [C] is the type of the carrier, e.g. a mutable HTTP request builder.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#textmap-propagator
 */
@ExperimentalApi
public fun interface TextMapSetter<C> {

    /**
     * Sets [key] to [value] on [carrier], replacing any existing value.
     * Values must consist only of US-ASCII characters valid for HTTP headers.
     */
    public fun set(carrier: C, key: String, value: String)
}
