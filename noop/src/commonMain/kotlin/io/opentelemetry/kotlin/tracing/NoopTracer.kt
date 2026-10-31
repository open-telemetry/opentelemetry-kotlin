package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

@ExperimentalApi
internal object NoopTracer : Tracer {

    override fun enabled(): Boolean = false

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span {
        val parentSpanContext = runCatching { parentContext?.extractSpan()?.spanContext }.getOrNull()
        return when {
            parentSpanContext?.isValid == true -> createNonRecordingSpan(parentSpanContext)
            else -> NoopSpan
        }
    }
}
