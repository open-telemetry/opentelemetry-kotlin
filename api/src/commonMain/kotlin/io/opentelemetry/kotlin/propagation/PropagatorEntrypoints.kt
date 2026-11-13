package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Returns a [TextMapPropagator] that sequentially delegates to each of [propagators]. Injection
 * invokes every delegate in order, extraction threads the context through each delegate in order,
 * and the declared fields are the de-duplicated union of each delegate's fields. If a delegate
 * throws, it is skipped and the remaining delegates still run.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance. It doesn't consult
 * [io.opentelemetry.kotlin.OpenTelemetry.propagator], so it keeps working when the SDK is disabled.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#composite-propagator
 */
@ExperimentalApi
public fun createCompositePropagator(vararg propagators: TextMapPropagator): TextMapPropagator =
    CompositeTextMapPropagator(propagators.map(TextMapPropagator::guarded))

/**
 * Returns a [TextMapPropagator] that injects and extracts [io.opentelemetry.kotlin.baggage.Baggage]
 * using the W3C Baggage `baggage` header.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance. It doesn't consult
 * [io.opentelemetry.kotlin.OpenTelemetry.propagator], so it keeps working when the SDK is disabled.
 *
 * https://www.w3.org/TR/baggage/
 */
@ExperimentalApi
public fun createW3CBaggagePropagator(): TextMapPropagator = GUARDED_W3C_BAGGAGE

/**
 * Returns a [TextMapPropagator] that injects and extracts the
 * [io.opentelemetry.kotlin.tracing.SpanContext] of the current span using the W3C Trace Context
 * `traceparent` and `tracestate` headers.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance. It doesn't consult
 * [io.opentelemetry.kotlin.OpenTelemetry.propagator], so it keeps working when the SDK is disabled.
 *
 * https://www.w3.org/TR/trace-context/
 */
@ExperimentalApi
public fun createW3CTraceContextPropagator(): TextMapPropagator = GUARDED_W3C_TRACE_CONTEXT

/**
 * Returns a [TextMapPropagator] that injects nothing, extracts nothing (returning the supplied
 * context unchanged) and declares no fields.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance. It doesn't consult
 * [io.opentelemetry.kotlin.OpenTelemetry.propagator], so it keeps working when the SDK is disabled.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#global-propagators
 */
@ExperimentalApi
public fun createNoopPropagator(): TextMapPropagator = NoopTextMapPropagator

@OptIn(ExperimentalApi::class)
private val GUARDED_W3C_BAGGAGE: TextMapPropagator = W3CBaggagePropagator.guarded()

@OptIn(ExperimentalApi::class)
private val GUARDED_W3C_TRACE_CONTEXT: TextMapPropagator = W3CTraceContextPropagator.guarded()
