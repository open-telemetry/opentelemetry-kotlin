package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.BaggageFactoryImpl

@OptIn(ExperimentalApi::class)
private val factory = BaggageFactoryImpl()

/**
 * Creates a [Baggage] by configuring entries inside the [action] DSL block. Omitting [action]
 * returns the empty [Baggage].
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance: the Baggage API is
 * fully functional in the absence of an installed SDK.
 *
 * https://opentelemetry.io/docs/specs/otel/baggage/api/
 */
@ExperimentalApi
public fun createBaggage(action: BaggageCreationAction.() -> Unit = {}): Baggage = factory.create(action)
