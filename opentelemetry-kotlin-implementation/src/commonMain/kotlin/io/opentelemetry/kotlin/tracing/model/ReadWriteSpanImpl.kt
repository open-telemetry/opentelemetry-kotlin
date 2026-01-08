package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * A view of [SpanModel] that is returned when read-write operations are permissible on a span.
 * Currently this is just in [io.opentelemetry.kotlin.tracing.export.SpanProcessor].
 */
@OptIn(ExperimentalApi::class)
internal class ReadWriteSpanImpl(private val model: SpanModel) : ReadWriteSpan by model
