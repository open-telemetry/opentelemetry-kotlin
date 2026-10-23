package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.factory.ContextFactory

internal class PropagatingNoopTracer(
    private val contextFactory: ContextFactory,
    private val sdkErrorHandler: SdkErrorHandler,
) : Tracer {

    private val root = contextFactory.root()
    private val invalidSpanContext = createInvalidSpanContext()
    private val invalidSpan = createNonRecordingSpan(invalidSpanContext)

    override fun enabled(): Boolean = false

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span =
        sdkErrorHandler.sdkGuardOrDefault(invalidSpan, "Tracer.startSpan failed") {
            val ctx = parentContext ?: contextFactory.implicit()
            if (ctx == root) {
                return@sdkGuardOrDefault invalidSpan
            }
            val parentSpanContext = ctx.extractSpan().spanContext
            when {
                parentSpanContext.isValid -> createNonRecordingSpan(parentSpanContext)
                else -> invalidSpan
            }
        }
}
