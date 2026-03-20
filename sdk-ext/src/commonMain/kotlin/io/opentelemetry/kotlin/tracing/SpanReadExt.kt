package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

/**
 * Syntactic sugar for converting a [Span] to a [ReadableSpan].
 *
 * Note: this cast will only succeed when using the KMP implementation of [io.opentelemetry.kotlin.OpenTelemetry].
 */
@ExperimentalApi
public fun Span.toReadableSpan(): ReadableSpan = this as ReadableSpan
