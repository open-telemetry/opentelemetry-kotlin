package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.copyTypedAttributes
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.export.ShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.DefaultTraceFlagsFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.propagation.utils.isValidTraceIdBytes
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.model.CreatedSpan
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanImpl
import io.opentelemetry.kotlin.tracing.model.SpanCreationCollector
import io.opentelemetry.kotlin.tracing.model.SpanModel
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult

internal class TracerImpl(
    private val clock: Clock,
    private val processor: SpanProcessor?,
    private val contextFactory: ContextFactory,
    private val spanContextFactory: SpanContextFactory,
    private val idGenerator: IdGenerator,
    private val scope: InstrumentationScopeInfo,
    private val resource: Resource,
    private val spanLimitConfig: SpanLimitConfig,
    private val shutdownState: ShutdownState,
    private val sampler: Sampler = AlwaysOnSampler,
    private val sdkErrorHandler: SdkErrorHandler,
) : Tracer {

    private val noopSpan = NoopOpenTelemetry.tracerProvider.getTracer("").startSpan("")
    private val root = contextFactory.root()
    private val invalidSpanContext = spanContextFactory.invalid
    private val invalidSpan = NonRecordingSpan(invalidSpanContext, invalidSpanContext)

    override fun enabled(): Boolean =
        sdkErrorHandler.guardOrDefault(false, "Tracer.enabled failed") {
            !shutdownState.isShutdown && processor != null
        }

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanCreationAction.() -> Unit)?
    ): Span =
        sdkErrorHandler.guardOrDefault(invalidSpan, "Tracer.startSpan failed") {
            shutdownState.ifActiveOrElse(noopSpan) {
                if (name.isBlank()) {
                    return@ifActiveOrElse invalidSpan
                }

                val ctx = parentContext ?: contextFactory.implicit()

                val parentSpanContext = when (ctx) {
                    root -> invalidSpanContext
                    else -> ctx.extractSpan().spanContext
                }
                // only inherit parent trace ID if it matches the format
                val parentTraceIdBytes = parentSpanContext.traceIdBytes
                val inheritTraceId = parentSpanContext.isValid && parentTraceIdBytes.isValidTraceIdBytes()

                val traceIdBytes = when {
                    inheritTraceId -> parentTraceIdBytes
                    else -> idGenerator.generateTraceIdBytes()
                }
                val randomTraceId = when {
                    inheritTraceId -> parentSpanContext.traceFlags.isRandom
                    else -> idGenerator.generatesRandomTraceIds
                }
                val spanIdBytes = idGenerator.generateSpanIdBytes()

                val collector = SpanCreationCollector(spanLimitConfig)
                action?.invoke(collector)

                val result = sampler.shouldSample(
                    context = ctx,
                    traceIdBytes = traceIdBytes,
                    name = name,
                    spanKind = spanKind,
                    attributes = collector.attributes,
                    links = collector.links
                )

                val sampled = result.decision == SamplingResult.Decision.RECORD_AND_SAMPLE
                val spanContext = calculateSpanContext(
                    traceIdBytes = traceIdBytes,
                    spanIdBytes = spanIdBytes,
                    sampled = sampled,
                    randomTraceId = randomTraceId,
                    traceState = result.traceState,
                )

                if (result.decision == SamplingResult.Decision.DROP) {
                    return@ifActiveOrElse NonRecordingSpan(parentSpanContext, spanContext)
                }

                val spanModel = SpanModel(
                    clock = clock,
                    processor = processor,
                    name = name,
                    spanKind = spanKind,
                    startTimestamp = startTimestamp?.takeIf { it > 0 } ?: clock.now(),
                    instrumentationScopeInfo = scope,
                    resource = resource,
                    parent = parentSpanContext,
                    spanContext = spanContext,
                    spanLimitConfig = spanLimitConfig,
                    initialLinks = collector.links,
                    initialDroppedAttributesCount = collector.droppedAttributesCount,
                    initialDroppedLinksCount = collector.droppedLinksCount,
                    sdkErrorHandler = sdkErrorHandler
                )
                spanModel.copyTypedAttributes(collector.attributes.attributes)
                spanModel.copyTypedAttributes(result.attributes.attributes)
                sdkErrorHandler.guard {
                    processor?.takeIf(SpanProcessor::isStartRequired)
                        ?.onStart(ReadWriteSpanImpl(spanModel), ctx)
                }
                CreatedSpan(spanModel)
            }
        }

    private fun calculateSpanContext(
        traceIdBytes: ByteArray,
        spanIdBytes: ByteArray,
        sampled: Boolean,
        randomTraceId: Boolean,
        traceState: TraceState,
    ): SpanContext {
        // invalid IDs are replaced with all zeros
        return spanContextFactory.create(
            traceIdBytes = traceIdBytes,
            spanIdBytes = spanIdBytes,
            traceFlags = DefaultTraceFlagsFactory.create(isSampled = sampled, isRandom = randomTraceId),
            isRemote = false,
            traceState = traceState,
        )
    }
}
