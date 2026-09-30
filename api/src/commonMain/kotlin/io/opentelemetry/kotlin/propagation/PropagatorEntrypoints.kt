package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Returns a [TextMapPropagator] that sequentially delegates to each of [propagators]. Injection
 * invokes every delegate in order, extraction threads the context through each delegate in order,
 * and the declared fields are the de-duplicated union of each delegate's fields.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#composite-propagator
 */
@ExperimentalApi
public fun createCompositePropagator(vararg propagators: TextMapPropagator): TextMapPropagator =
    CompositeTextMapPropagator(propagators.toList())

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

/**
 * Returns a [TextMapPropagator] that injects nothing, extracts nothing (returning the supplied
 * context unchanged) and declares no fields.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#global-propagators
 */
@ExperimentalApi
public fun createNoopPropagator(): TextMapPropagator = NoopTextMapPropagator
