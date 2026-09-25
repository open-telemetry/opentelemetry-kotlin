package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageCreationAction

/**
 * A factory for creating [Baggage] instances.
 *
 * https://opentelemetry.io/docs/specs/otel/baggage/api/
 */
@ExperimentalApi
public interface BaggageFactory {

    /**
     * Returns the empty [Baggage].
     */
    public fun empty(): Baggage

    /**
     * Creates a [Baggage] by configuring entries inside the [action] DSL block.
     */
    public fun create(action: BaggageCreationAction.() -> Unit): Baggage
}
