package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuardOrDefault

internal class PropagatingNoopTracer(
    private val sdkErrorHandler: SdkErrorHandler,
) : Tracer {

    private val root = rootContext()
    private val invalidSpanContext = createInvalidSpanContext()
    private val invalidSpan = createNonRecordingSpan(invalidSpanContext)

    override fun enabled(): Boolean = false

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long,
        action: (SpanCreationAction.() -> Unit)?
    ): Span =
        sdkErrorHandler.sdkGuardOrDefault(invalidSpan, "Tracer.startSpan failed") {
            val ctx = parentContext ?: currentContext()
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
