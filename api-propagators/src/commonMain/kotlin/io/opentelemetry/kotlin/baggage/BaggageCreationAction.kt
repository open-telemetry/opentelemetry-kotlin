package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * DSL receiver for assembling a [Baggage] instance.
 *
 * Obtained via [io.opentelemetry.kotlin.factory.BaggageFactory.create]; callers configure
 * entries using [put] and [remove] inside the DSL block. The resulting [Baggage] is returned
 * by the factory once the block completes.
 *
 * https://opentelemetry.io/docs/specs/otel/baggage/api/#baggage-builder
 */
@ExperimentalApi
public interface BaggageCreationAction {

    /**
     * Adds an entry to the baggage. If [name] already has an entry, its value and
     * metadata are replaced.
     *
     * [metadata] is the raw metadata string per the OpenTelemetry specification; an
     * empty string indicates no metadata.
     */
    public fun put(name: String, value: String, metadata: String = "")

    /**
     * Removes the entry for [name], if present.
     */
    public fun remove(name: String)
}
