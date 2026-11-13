package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.currentContext

@ExperimentalApi
internal object NoopTracer : Tracer {

    override fun enabled(): Boolean = false

    /**
     * Returns a non-recording span that carries the [SpanContext] of the parent, taken from
     * [parentContext] or, when that is null, the current context. A parent span that is already
     * non-recording is returned as is.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/api/#behavior-of-the-api-in-the-absence-of-an-installed-sdk
     */
    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long,
        action: (SpanCreationAction.() -> Unit)?
    ): Span = runCatching {
        val parentSpan = (parentContext ?: currentContext()).extractSpan()
        val parentSpanContext = parentSpan.spanContext
        when {
            !parentSpanContext.isValid -> NoopSpan
            !parentSpan.isRecording() -> parentSpan
            else -> createNonRecordingSpan(parentSpanContext)
        }
    }.getOrDefault(NoopSpan)
}
