package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import io.opentelemetry.kotlin.tracing.model.Span

/**
 * Syntactic sugar for converting a [Span] to a [ReadableSpan].
 */
internal fun Span.toReadableSpan() = this as ReadableSpan
