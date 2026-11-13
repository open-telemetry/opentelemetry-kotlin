package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.B3Format

/**
 * Returns a [TextMapPropagator] that injects and extracts the
 * [io.opentelemetry.kotlin.tracing.SpanContext] of the current span using B3 headers. Extraction
 * accepts both the single `b3` header and the multi `X-B3-*` headers. Injection uses whatever
 * was supplied to [format].
 *
 * https://github.com/openzipkin/b3-propagation
 */
@ExperimentalApi
public fun createB3Propagator(format: B3Format = B3Format.SINGLE): TextMapPropagator = when (format) {
    B3Format.SINGLE -> GUARDED_B3_SINGLE
    B3Format.MULTI -> GUARDED_B3_MULTI
}

@OptIn(ExperimentalApi::class)
private val GUARDED_B3_SINGLE: TextMapPropagator = GuardedB3Propagator(B3Propagator.SINGLE_HEADER)

@OptIn(ExperimentalApi::class)
private val GUARDED_B3_MULTI: TextMapPropagator = GuardedB3Propagator(B3Propagator.MULTI_HEADER)
