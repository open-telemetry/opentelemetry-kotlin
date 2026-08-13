package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.export.ShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.SPAN_ID_BYTES
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.TRACE_ID_BYTES
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.isValidSpanIdBytes
import io.opentelemetry.kotlin.factory.isValidTraceIdBytes
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
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
    spanContextFactory: SpanContextFactory,
    traceFlagsFactory: TraceFlagsFactory,
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
    private val sampledFlags = traceFlagsFactory.default
    private val sampledRandomFlags = TraceFlagsImpl(isSampled = true, isRandom = true)
    private val unsampledFlags = TraceFlagsImpl(isSampled = false, isRandom = false)
    private val unsampledRandomFlags = TraceFlagsImpl(isSampled = false, isRandom = true)

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
                val remoteParent = inheritTraceId && parentSpanContext.isRemote
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
                    remoteParent = remoteParent,
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
                    startTimestamp = startTimestamp ?: clock.now(),
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
                spanModel.setAttributes(result.attributes)
                spanModel.setAttributes(collector.attributes)
                sdkErrorHandler.guard {
                    processor?.onStart(ReadWriteSpanImpl(spanModel), ctx)
                }
                CreatedSpan(spanModel)
            }
        }

    private fun calculateSpanContext(
        traceIdBytes: ByteArray,
        spanIdBytes: ByteArray,
        sampled: Boolean,
        randomTraceId: Boolean,
        remoteParent: Boolean,
        traceState: TraceState,
    ): SpanContext {
        val validTraceId = traceIdBytes.isValidTraceIdBytes()
        val validSpanId = spanIdBytes.isValidSpanIdBytes()

        return SpanContextImpl(
            // replace invalid IDs with all zeros
            traceIdBytes = when {
                validTraceId -> traceIdBytes
                else -> ByteArray(TRACE_ID_BYTES)
            },
            spanIdBytes = when {
                validSpanId -> spanIdBytes
                else -> ByteArray(SPAN_ID_BYTES)
            },
            traceFlags = when {
                sampled && randomTraceId -> sampledRandomFlags
                sampled -> sampledFlags
                randomTraceId -> unsampledRandomFlags
                else -> unsampledFlags
            },
            isValid = validTraceId && validSpanId,
            isRemote = remoteParent,
            traceState = traceState,
        )
    }
}
