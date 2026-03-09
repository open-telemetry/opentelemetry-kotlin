package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanCreationAction
import io.opentelemetry.kotlin.tracing.model.SpanKind

class FakeTracer(
    val name: String
) : Tracer {

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span = FakeSpan()
}
