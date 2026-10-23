package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Wraps a [SpanContext] in a [Span] that does not record any data. All mutating operations
 * are no-ops and [Span.isRecording] returns false. This is useful for propagating a [SpanContext]
 * (e.g. one extracted from a remote parent) where a [Span] is required.
 *
 * [parent] is reported as the span's parent and defaults to the invalid [SpanContext].
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#wrapping-a-spancontext-in-a-span
 */
@ExperimentalApi
public fun createNonRecordingSpan(
    spanContext: SpanContext,
    parent: SpanContext = createInvalidSpanContext(),
): Span = NonRecordingSpan(parent, spanContext)
