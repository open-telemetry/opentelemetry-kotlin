package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.context.Context

class FakeTracer(
    val name: String,
    var enabledResult: () -> Boolean = { true },
) : Tracer {

    override fun enabled(): Boolean = enabledResult()

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span = FakeSpan()
}
