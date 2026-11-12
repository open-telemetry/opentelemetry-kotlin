package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.copyAttributes
import io.opentelemetry.kotlin.attributes.typedAttributesOrConvert
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.export.ShutdownState
import io.opentelemetry.kotlin.factory.IdGenerator
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
    private val idGenerator: IdGenerator,
    private val scope: InstrumentationScopeInfo,
    private val resource: Resource,
    private val spanLimitConfig: SpanLimitConfig,
    private val shutdownState: ShutdownState,
    private val sampler: Sampler = AlwaysOnSampler,
    private val sdkErrorHandler: SdkErrorHandler,
) : Tracer {

    private val propagatingNoopTracer = PropagatingNoopTracer(sdkErrorHandler)
    private val root = rootContext()
    private val invalidSpanContext = createInvalidSpanContext()
    private val invalidSpan = createNonRecordingSpan(invalidSpanContext)

    override fun enabled(): Boolean =
        sdkErrorHandler.sdkGuardOrDefault(false, "Tracer.enabled failed") {
            !shutdownState.isShutdown && processor != null
        }

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long,
        action: (SpanCreationAction.() -> Unit)?
    ): Span =
        sdkErrorHandler.sdkGuardOrDefault(invalidSpan, "Tracer.startSpan failed") {
            if (shutdownState.isShutdown) {
                return@sdkGuardOrDefault propagatingNoopTracer.startSpan(
                    name = name,
                    parentContext = parentContext,
                    spanKind = spanKind,
                    startTimestamp = startTimestamp,
                    action = action,
                )
            }
            if (name.isBlank()) {
                return@sdkGuardOrDefault invalidSpan
            }

            val ctx = parentContext ?: currentContext()

            val parentSpanContext = when (ctx) {
                root -> invalidSpanContext
                else -> ctx.extractSpan().spanContext
            }
            // only inherit parent trace ID if it matches the format
            val parentTraceIdBytes = parentSpanContext.traceIdBytes
            val inheritTraceId = parentSpanContext.isValid && parentTraceIdBytes.isValidTraceIdBytes()

            val traceIdBytes = when {
                inheritTraceId -> parentTraceIdBytes
                else -> userCode { idGenerator.generateTraceIdBytes() }
            }
            val randomTraceId = when {
                inheritTraceId -> parentSpanContext.traceFlags.isRandom
                else -> userCode { idGenerator.generatesRandomTraceIds }
            }
            val spanIdBytes = userCode { idGenerator.generateSpanIdBytes() }

            val collector = SpanCreationCollector(spanLimitConfig)
            userCode { action?.invoke(collector) }

            val result = userCode {
                sampler.shouldSample(
                    context = ctx,
                    traceIdBytes = traceIdBytes,
                    name = name,
                    spanKind = spanKind,
                    attributes = collector.attributes,
                    links = collector.links
                )
            }

            val decision = userCode { result.decision }
            val sampled = decision == SamplingResult.Decision.RECORD_AND_SAMPLE
            val spanContext = calculateSpanContext(
                traceIdBytes = traceIdBytes,
                spanIdBytes = spanIdBytes,
                sampled = sampled,
                randomTraceId = randomTraceId,
                samplerTraceState = userCode { result.traceState },
            )

            if (decision == SamplingResult.Decision.DROP) {
                return@sdkGuardOrDefault createNonRecordingSpan(spanContext, parent = parentSpanContext)
            }

            val spanModel = SpanModel(
                clock = clock,
                processor = processor,
                name = name,
                spanKind = spanKind,
                startTimestamp = when {
                    startTimestamp > 0 -> startTimestamp
                    else -> userCode { clock.now() }
                },
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
            spanModel.copyAttributes(collector.attributes)
            spanModel.copyAttributes(userCode { result.attributes.typedAttributesOrConvert() })
            sdkErrorHandler.guard {
                processor?.takeIf(SpanProcessor::isStartRequired)
                    ?.onStart(ReadWriteSpanImpl(spanModel), ctx)
            }
            CreatedSpan(spanModel)
        }

    private fun calculateSpanContext(
        traceIdBytes: ByteArray,
        spanIdBytes: ByteArray,
        sampled: Boolean,
        randomTraceId: Boolean,
        samplerTraceState: TraceState,
    ): SpanContext {
        // invalid IDs are replaced with all zeros
        return createSpanContext(traceIdBytes, spanIdBytes) {
            isSampled = sampled
            isRandom = randomTraceId
            val entries = samplerTraceState.asMap()
            if (entries.isNotEmpty()) {
                traceState {
                    entries.forEach { (key, value) -> put(key, value) }
                }
            }
        }
    }
}
