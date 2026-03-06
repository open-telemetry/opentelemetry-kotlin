package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanCreationAction
import io.opentelemetry.kotlin.tracing.model.SpanKind

@ExperimentalApi
internal object NoopTracer : Tracer {

    @Deprecated(
        "Deprecated.",
        replaceWith = ReplaceWith("startSpan(name, parentContext, spanKind, startTimestamp, action)")
    )
    override fun createSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span = NoopSpan

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span = NoopSpan
}
