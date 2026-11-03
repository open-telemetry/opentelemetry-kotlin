package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.B3Format

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
 * Returns a [TextMapPropagator] that injects and extracts the
 * [io.opentelemetry.kotlin.tracing.SpanContext] of the current span using B3 headers. Extraction
 * accepts both the single `b3` header and the multi `X-B3-*` headers. Injection uses whatever
 * was supplied to [format].
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance. It doesn't consult
 * [io.opentelemetry.kotlin.OpenTelemetry.propagator], so it keeps working when the SDK is disabled.
 *
 * https://github.com/openzipkin/b3-propagation
 */
@ExperimentalApi
public fun createB3Propagator(format: B3Format = B3Format.SINGLE): TextMapPropagator = when (format) {
    B3Format.SINGLE -> GUARDED_B3_SINGLE
    B3Format.MULTI -> GUARDED_B3_MULTI
}

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

@OptIn(ExperimentalApi::class)
private val GUARDED_B3_SINGLE: TextMapPropagator = B3Propagator.SINGLE_HEADER.guarded()

@OptIn(ExperimentalApi::class)
private val GUARDED_B3_MULTI: TextMapPropagator = B3Propagator.MULTI_HEADER.guarded()
