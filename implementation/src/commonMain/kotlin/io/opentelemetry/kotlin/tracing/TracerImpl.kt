package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.ShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.model.CreatedSpan
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanImpl
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanModel
import io.opentelemetry.kotlin.tracing.model.SpanRelationships

internal class TracerImpl(
    private val clock: Clock,
    private val processor: SpanProcessor?,
    private val contextFactory: ContextFactory,
    spanContextFactory: SpanContextFactory,
    traceFlagsFactory: TraceFlagsFactory,
    traceStateFactory: TraceStateFactory,
    private val spanFactory: SpanFactory,
    private val idGenerator: IdGenerator,
    private val scope: InstrumentationScopeInfo,
    private val resource: Resource,
    private val spanLimitConfig: SpanLimitConfig,
    private val shutdownState: ShutdownState,
) : Tracer {

    private val noopSpan = NoopOpenTelemetry.tracerProvider.getTracer("").startSpan("")
    private val root = contextFactory.root()
    private val invalidSpanContext = spanContextFactory.invalid
    private val traceFlagsDefault = traceFlagsFactory.default
    private val traceStateDefault = traceStateFactory.default

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanRelationships.() -> Unit)?
    ): Span =
        shutdownState.ifActiveOrElse(noopSpan) {
            val ctx = parentContext ?: contextFactory.implicit()

            val parentSpanContext = when (ctx) {
                root -> invalidSpanContext
                else -> spanFactory.fromContext(ctx).spanContext
            }

            val spanContext = calculateSpanContext(parentSpanContext)

            val spanModel = SpanModel(
                clock = clock,
                processor = processor,
                name = name,
                spanKind = spanKind,
                startTimestamp = startTimestamp ?: clock.now(),
                instrumentationScopeInfo = scope,
                resource = resource,
                parent = parentSpanContext,
                spanContext = spanContext,
                spanLimitConfig = spanLimitConfig
            )
            if (action != null) {
                action(spanModel)
            }
            processor?.onStart(ReadWriteSpanImpl(spanModel), ctx)
            CreatedSpan(spanModel)
        }

    private fun calculateSpanContext(parent: SpanContext): SpanContext {
        val traceId = if (parent.isValid) {
            parent.traceIdBytes
        } else {
            idGenerator.generateTraceIdBytes()
        }
        val spanId = idGenerator.generateSpanIdBytes()

        return SpanContextImpl(
            traceIdBytes = traceId,
            spanIdBytes = spanId,
            traceFlags = traceFlagsDefault,
            isValid = true,
            isRemote = false,
            traceState = traceStateDefault,
        )
    }
}
