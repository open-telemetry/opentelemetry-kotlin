package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageCreationAction
import io.opentelemetry.kotlin.baggage.createBaggage

/**
 * Exposes the API's [createBaggage] through [BaggageFactory] so that the SDK and standalone code
 * share the same Baggage implementation.
 */
@OptIn(ExperimentalApi::class)
public object DefaultBaggageFactory : BaggageFactory {
    override fun empty(): Baggage = createBaggage()
    override fun create(action: BaggageCreationAction.() -> Unit): Baggage = createBaggage(action)
}
