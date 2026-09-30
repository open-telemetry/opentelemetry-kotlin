package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Returns a [TextMapPropagator] that injects and extracts [io.opentelemetry.kotlin.baggage.Baggage]
 * using the W3C Baggage `baggage` header.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://www.w3.org/TR/baggage/
 */
@ExperimentalApi
public fun createW3CBaggagePropagator(): TextMapPropagator = W3CBaggagePropagator
