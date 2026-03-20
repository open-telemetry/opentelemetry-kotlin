package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.tracing.Span

/**
 * A view of [SpanModel] that is returned after creating a span. State is largely read-only,
 * excepting the ability to add links, events, attributes, and alter name/status. Resource/scope
 * information is not available via the [io.opentelemetry.kotlin.tracing.Span] interface but is accessible by casting to
 * [ReadableSpan].
 */
internal class CreatedSpan(private val model: SpanModel) : Span by model, ReadableSpan by model {
    override val spanContext = model.spanContext
    override val parent = model.parent
}
