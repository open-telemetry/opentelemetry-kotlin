package io.opentelemetry.kotlin.tracing.model
/**
 * A view of [SpanModel] that is returned after creating a span. State is largely read-only,
 * excepting the ability to add links, events, attributes, and alter name/status. Resource/scope
 * information is not available.
 */
internal class CreatedSpan(private val model: SpanModel) : Span by model
