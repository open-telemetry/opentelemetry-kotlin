package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.B3Format

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
 * Returns a [TextMapPropagator] that injects and extracts the
 * [io.opentelemetry.kotlin.tracing.SpanContext] of the current span using the W3C Trace Context
 * `traceparent` and `tracestate` headers.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://www.w3.org/TR/trace-context/
 */
@ExperimentalApi
public fun createW3CTraceContextPropagator(): TextMapPropagator = W3CTraceContextPropagator

/**
 * Returns a [TextMapPropagator] that injects and extracts the
 * [io.opentelemetry.kotlin.tracing.SpanContext] of the current span using B3 headers. Extraction
 * accepts both the single `b3` header and the multi `X-B3-*` headers. Injection uses whatever
 * was supplied to [format].
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://github.com/openzipkin/b3-propagation
 */
@ExperimentalApi
public fun createB3Propagator(format: B3Format = B3Format.SINGLE): TextMapPropagator = when (format) {
    B3Format.SINGLE -> B3Propagator.SINGLE_HEADER
    B3Format.MULTI -> B3Propagator.MULTI_HEADER
}

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
