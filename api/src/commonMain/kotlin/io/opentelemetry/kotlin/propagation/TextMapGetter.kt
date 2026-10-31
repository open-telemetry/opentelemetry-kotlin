package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Reads propagated fields from a carrier.
 *
 * [C] is the type of the carrier, e.g. an HTTP request object.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#textmap-propagator
 */
@ExperimentalApi
public interface TextMapGetter<C> {

    /**
     * Returns all keys present in [carrier].
     * */
    public fun keys(carrier: C): Collection<String>

    /**
     * Returns the first value for [key] in [carrier], or null if absent.
     * Key lookup must be case-insensitive for HTTP carriers.
     */
    public fun get(carrier: C, key: String): String?

    /**
     * Returns all values associated with [key] in [carrier], in the order they appear,
     * or an empty list if absent.
     */
    public fun getAll(carrier: C, key: String): List<String>
}
