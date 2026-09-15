package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import java.util.concurrent.TimeUnit

internal class TracerAdapter(
    private val tracer: OtelJavaTracer,
    private val clock: Clock,
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
        val start = startTimestamp ?: clock.now()
        val parentCtx = (parentContext ?: contextFactory.implicit()).toOtelJavaContext()

        val span = tracer.spanBuilder(name)
            .setSpanKind(spanKind.toOtelJavaSpanKind())
            .setStartTimestamp(start, TimeUnit.NANOSECONDS)
            .setParent(parentCtx)
            .startSpan()

        return SpanAdapter(
            impl = span,
            clock = clock,
            parentCtx = parentCtx,
            spanKind = spanKind,
            startTimestamp = start,
            spanLimitsConfig = spanLimitsConfig,
        ).apply {
            setName(name)
            if (action != null) {
                action(this)
            }
        }
    }
}
