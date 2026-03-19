package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.ShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.model.CreatedSpan
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanImpl
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanCreationAction
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanModel
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult

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
    private val sampler: Sampler = AlwaysOnSampler(spanFactory),
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
        action: (SpanCreationAction.() -> Unit)?
    ): Span =
        shutdownState.ifActiveOrElse(noopSpan) {
            val ctx = parentContext ?: contextFactory.implicit()

            val parentSpanContext = when (ctx) {
                root -> invalidSpanContext
                else -> spanFactory.fromContext(ctx).spanContext
            }
            val traceIdBytes = when {
                parentSpanContext.isValid -> parentSpanContext.traceIdBytes
                else -> idGenerator.generateTraceIdBytes()
            }

            val samplingDecision = sampler.shouldSample(
                context = ctx,
                traceId = traceIdBytes.toHexString(),
                name = name,
                spanKind = spanKind,
                attributes = AttributesModel(),
                links = emptyList(),
            ).decision

            if (samplingDecision == SamplingResult.Decision.DROP) {
                return@ifActiveOrElse noopSpan
            }

            val isSampled = samplingDecision == SamplingResult.Decision.RECORD_AND_SAMPLE
            val spanContext = calculateSpanContext(traceIdBytes, isSampled)

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

    private fun calculateSpanContext(traceIdBytes: ByteArray, isSampled: Boolean = true): SpanContext {
        val spanId = idGenerator.generateSpanIdBytes()

        return SpanContextImpl(
            traceIdBytes = traceIdBytes,
            spanIdBytes = spanId,
            traceFlags = when {
                isSampled -> traceFlagsDefault
                else -> TraceFlagsImpl(isSampled = false, isRandom = false)
            },
            isValid = true,
            isRemote = false,
            traceState = traceStateDefault,
        )
    }
}
