package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

/**
 * Syntactic sugar for converting a [Span] to a [ReadableSpan].
 *
 * Returns a no-op readable span when this span does not implement [ReadableSpan].
 */
@ExperimentalApi
public fun Span.toReadableSpan(): ReadableSpan = this as? ReadableSpan ?: NoopReadableSpan
