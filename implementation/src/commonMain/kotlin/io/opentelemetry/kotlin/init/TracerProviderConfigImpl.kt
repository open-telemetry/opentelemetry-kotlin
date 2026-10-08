package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.dsl.ResourceConfigDslImpl
import io.opentelemetry.kotlin.config.dsl.SpanLimitsConfigDslImpl
import io.opentelemetry.kotlin.config.dsl.TraceExportConfigDslImpl
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.TracerConfigImpl
import io.opentelemetry.kotlin.tracing.TracerConfigurator
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.stdoutSpanExporter
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.toSampler

internal class TracerProviderConfigImpl(
    private val clock: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
    private val resourceConfig: ResourceConfigDslImpl = ResourceConfigDslImpl()
) : TracerProviderConfigDsl, ResourceConfigDsl by resourceConfig {

    private var processor: SpanProcessor? = null
    private var samplerAction: (SamplerConfigDsl.() -> Sampler)? = null
    private val defaultTracerConfig = TracerConfigImpl(true)
    private var tracerConfigurator: TracerConfigurator = TracerConfigurator {
        defaultTracerConfig
    }
    private val spanLimits = SpanLimitsConfigDslImpl()

    override fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit) {
        spanLimits.action()
    }

    override fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        if (processor != null) {
            sdkErrorHandler.reportError(
                SdkError.ApiMisuse(
                    api = "TracerProviderConfigDsl.export",
                    message = "export() should only be called once.",
                    severity = SdkErrorSeverity.WARNING,
                )
            )
            return
        }
        processor = TraceExportConfigDslImpl(clock, sdkErrorHandler).action()
    }

    override fun sampler(action: SamplerConfigDsl.() -> Sampler) {
        samplerAction = action
    }

    override fun tracerConfigurator(configurator: TracerConfigurator) {
        tracerConfigurator = configurator
    }

    fun generateTracingConfig(
        base: Resource,
        processorBehavior: SpanProcessorBehavior? = null,
    ): TracingConfig {
        val action = samplerAction ?: { parentBased(root = alwaysOn()) }
        return TracingConfig(
            processor = processor ?: processorFromBehavior(processorBehavior),
            resource = base.merge(resourceConfig.toBehavior().toResource()),
            sdkErrorHandler = sdkErrorHandler,
            samplerFactory = { spanFactory -> SamplerConfigImpl(spanFactory).action() },
            tracerConfigurator = tracerConfigurator,
        )
    }

    internal fun applyResolvedSampler(behavior: SamplerBehavior?) {
        if (samplerAction != null || behavior == null) {
            return
        }
        samplerAction = { toSampler(behavior) }
    }

    fun toBehavior(): TracerProviderBehavior =
        TracerProviderBehavior(
            spanLimits = spanLimits.toBehavior()
        )

    private fun processorFromBehavior(processorBehavior: SpanProcessorBehavior?): SpanProcessor? {
        if (processorBehavior?.console == null) {
            return null
        }
        return TraceExportConfigDslImpl(clock, sdkErrorHandler).run {
            val exporter = stdoutSpanExporter()
            val batch = processorBehavior.batch
            if (batch == null) {
                simpleSpanProcessor(exporter)
            } else {
                batchSpanProcessor(
                    exporter = exporter,
                    scheduleDelayMs = batch.scheduleDelay ?: BatchTelemetryDefaults.SPAN_SCHEDULE_DELAY_MS,
                    exportTimeoutMs = batch.exportTimeout ?: BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
                    maxQueueSize = batch.maxQueueSize ?: BatchTelemetryDefaults.MAX_QUEUE_SIZE,
                    maxExportBatchSize = batch.maxExportBatchSize ?: BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
                )
            }
        }
    }
}
