package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * An immutable set of name/value pairs that can be propagated across process boundaries.
 *
 * All mutating operations return a new [Baggage] instance, leaving the original unchanged.
 *
 * https://opentelemetry.io/docs/specs/otel/baggage/api/
 */
@ExperimentalApi
@ThreadSafe
public interface Baggage {

    /**
     * Returns the value associated with [name], or null if [name] is not present.
     */
    @ThreadSafe
    public fun getValue(name: String): String?

    /**
     * Returns all baggage entries as an immutable map keyed by entry name.
     */
    @ThreadSafe
    public fun asMap(): Map<String, BaggageEntry>

    /**
     * Returns a new [Baggage] containing the given [name]/[value] pair with [metadata].
     *
     * [metadata] is the raw metadata string per the OpenTelemetry specification; an empty string
     * indicates no metadata. If an entry for [name] already exists its value and metadata are
     * replaced.
     */
    @ThreadSafe
    public fun put(name: String, value: String, metadata: String = ""): Baggage

    /**
     * Returns a new [Baggage] without the entry for [name].
     *
     * If [name] is not present the same [Baggage] instance is returned.
     */
    @ThreadSafe
    public fun remove(name: String): Baggage
}
