package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.tracing.model.ReadableSpan

/**
 * Syntactic sugar for converting a [Span] to a [ReadableSpan].
 */
internal fun Span.toReadableSpan() = this as ReadableSpan
