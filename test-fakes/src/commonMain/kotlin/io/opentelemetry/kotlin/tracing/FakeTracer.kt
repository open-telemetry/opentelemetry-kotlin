package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.context.Context

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
