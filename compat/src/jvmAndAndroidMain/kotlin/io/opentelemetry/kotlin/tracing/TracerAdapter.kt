package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.model.CompatSpanCreationState
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import java.util.concurrent.TimeUnit

internal class TracerAdapter(
    private val tracer: OtelJavaTracer,
    private val spanLimitsConfig: CompatSpanLimitsConfig,
    private val contextFactory: ContextFactory,
) : Tracer {

    override fun enabled(): Boolean = true

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span {
        val parentCtx = (parentContext ?: contextFactory.implicit()).toOtelJavaContext()

        val builder = tracer.spanBuilder(name)
            .setSpanKind(spanKind.toOtelJavaSpanKind())
            .setParent(parentCtx)
        // Left unset, the SDK stamps the start with the same clock it uses for end(). Filling it in
        // from [clock] would mix two clocks, skewing durations and ending short spans before they start.
        startTimestamp?.let { builder.setStartTimestamp(it, TimeUnit.NANOSECONDS) }

        val creationState = action?.let { CompatSpanCreationState(spanLimitsConfig).apply(it) }
        creationState?.applyTo(builder)

        return SpanAdapter(
            impl = builder.startSpan(),
            parentCtx = parentCtx,
            spanKind = spanKind,
            spanLimitsConfig = spanLimitsConfig,
            creationState = creationState,
        )
    }
}
