package io.opentelemetry.kotlin.tracing.model
/**
 * A view of [SpanModel] that is returned when only read operations are permissible on a span.
 * Currently this is just in [io.opentelemetry.kotlin.tracing.export.SpanProcessor].
 */
internal class ReadableSpanImpl(private val model: SpanModel) : ReadWriteSpan by model
