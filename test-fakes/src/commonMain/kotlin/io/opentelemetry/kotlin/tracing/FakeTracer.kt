package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanRelationships

class FakeTracer(
    val name: String
) : Tracer {

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanRelationships.() -> Unit)?
    ): Span = FakeSpan()
}
