package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.compat.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import io.opentelemetry.kotlin.tracing.model.CompatSpanCreationState
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import java.util.concurrent.TimeUnit

internal class TracerAdapter(
    private val tracer: OtelJavaTracer,
    private val spanLimitsConfig: CompatSpanLimitsConfig,
    private val contextFactory: ContextFactory,
    private val sdkErrorHandler: SdkErrorHandler,
) : Tracer {

    private val invalidSpan: Span by lazy {
        NonRecordingSpan(createInvalidSpanContext(), createInvalidSpanContext())
    }

    override fun enabled(): Boolean =
        sdkErrorHandler.sdkGuardOrDefault(false, "Tracer.enabled failed") {
            tracer.isEnabled
        }

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span {
        val parentCtx = sdkErrorHandler.sdkGuardOrDefault(null, "Tracer.startSpan failed") {
            (parentContext ?: contextFactory.implicit()).toOtelJavaContext()
        } ?: return invalidSpan

        return sdkErrorHandler.sdkGuardOrDefault(null, "Tracer.startSpan failed") {
            createSpan(name, parentCtx, spanKind, startTimestamp, action)
        } ?: fallbackSpan(parentCtx)
    }

    private fun createSpan(
        name: String,
        parentCtx: OtelJavaContext,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span {
        val builder = tracer.spanBuilder(name)
            .setSpanKind(spanKind.toOtelJavaSpanKind())
            .setParent(parentCtx)
        // Left unset, the SDK stamps the start with the same clock it uses for end(). Filling it in
        // from [clock] would mix two clocks, skewing durations and ending short spans before they start.
        startTimestamp?.let { builder.setStartTimestamp(it, TimeUnit.NANOSECONDS) }

        val creationState = action?.let { userCode { CompatSpanCreationState(spanLimitsConfig).apply(it) } }
        creationState?.applyTo(builder)

        return SpanAdapter(
            impl = builder.startSpan(),
            parentCtx = parentCtx,
            spanKind = spanKind,
            spanLimitsConfig = spanLimitsConfig,
            creationState = creationState,
            sdkErrorHandler = sdkErrorHandler,
        )
    }

    private fun fallbackSpan(parentCtx: OtelJavaContext): Span =
        sdkErrorHandler.sdkGuardOrDefault(invalidSpan) {
            val parent = OtelJavaSpan.fromContext(parentCtx).spanContext.toOtelKotlinSpanContext()
            NonRecordingSpan(parent, parent)
        }
}
