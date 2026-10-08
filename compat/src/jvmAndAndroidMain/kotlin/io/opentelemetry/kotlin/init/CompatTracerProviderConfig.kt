package io.opentelemetry.kotlin.init

import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaScopeConfigurator
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProviderBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProviderUtil
import io.opentelemetry.kotlin.aliases.OtelJavaTracerConfig
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.setTypedAttributes
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.dsl.SpanLimitsConfigDslImpl
import io.opentelemetry.kotlin.config.dsl.TraceExportConfigDslImpl
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.OtelJavaIdGeneratorAdapter
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.scope.toOtelKotlinInstrumentationScopeInfo
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.tracing.TracerConfigurator
import io.opentelemetry.kotlin.tracing.TracerProvider
import io.opentelemetry.kotlin.tracing.TracerProviderAdapter
import io.opentelemetry.kotlin.tracing.export.OtelJavaSpanProcessorAdapter
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.CompatSamplerConfig
import io.opentelemetry.kotlin.tracing.sampling.OtelJavaSamplerAdapter
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplerAdapter
import io.opentelemetry.kotlin.tracing.sampling.toSampler
import java.util.concurrent.TimeUnit
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor as OtelJavaBatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor as OtelJavaSimpleSpanProcessor

@ExperimentalApi
internal class CompatTracerProviderConfig(
    private val clock: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
) : TracerProviderConfigDsl {

    private val builder: OtelJavaSdkTracerProviderBuilder = OtelJavaSdkTracerProvider.builder()
    internal val spanLimitsConfig = CompatSpanLimitsConfig()
    private var tracerConfigurator: TracerConfigurator? = null
    private val resourceAttrs = CompatAttributesModel()
    private var resourceSchemaUrl: String? = null
    private val spanLimitsDsl = SpanLimitsConfigDslImpl()
    private var sampler: Sampler? = null
    private var exportConfigured = false

    override var serviceName: String? = null
        set(value) {
            field = value
            value?.let { resourceAttrs.setStringAttribute(ServiceAttributes.SERVICE_NAME, it) }
        }

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        resourceSchemaUrl = schemaUrl
        resourceAttrs.apply(attributes)
    }

    override fun resource(map: Map<String, Any>) {
        resourceAttrs.apply { setTypedAttributes(map) }
    }

    override fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit) {
        spanLimitsDsl.action()
    }

    override fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        exportConfigured = true
        val processor = TraceExportConfigDslImpl(clock, sdkErrorHandler).action()
        builder.addSpanProcessor(OtelJavaSpanProcessorAdapter(processor, sdkErrorHandler))
    }

    override fun sampler(action: SamplerConfigDsl.() -> Sampler) {
        sampler = buildSampler { action() }
    }

    internal fun applyResolvedSampler(behavior: SamplerBehavior?) {
        if (sampler != null || behavior == null) {
            return
        }
        sampler = buildSampler { toSampler(behavior) }
    }

    private fun buildSampler(action: SamplerConfigDsl.() -> Sampler): Sampler =
        sdkErrorHandler.guardOrDefault(defaultSampler, "Failed to create sampler, using default") {
            newSamplerDsl.action()
        }

    internal fun applyResolvedProcessor(behavior: SpanProcessorBehavior?) {
        if (exportConfigured || behavior?.console == null) {
            return
        }
        exportConfigured = true
        val exporter = LoggingSpanExporter.create()
        val batch = behavior.batch
        if (batch == null) {
            builder.addSpanProcessor(OtelJavaSimpleSpanProcessor.create(exporter))
        } else {
            val batchBuilder = OtelJavaBatchSpanProcessor.builder(exporter)
            batch.scheduleDelay?.let { batchBuilder.setScheduleDelay(it, TimeUnit.MILLISECONDS) }
            batch.exportTimeout?.let { batchBuilder.setExporterTimeout(it, TimeUnit.MILLISECONDS) }
            batch.maxQueueSize?.let { batchBuilder.setMaxQueueSize(it) }
            batch.maxExportBatchSize?.let { batchBuilder.setMaxExportBatchSize(it) }
            builder.addSpanProcessor(batchBuilder.build())
        }
    }

    private val newSamplerDsl: SamplerConfigDsl = CompatSamplerConfig(CompatSpanFactory())

    private val defaultSampler: Sampler = newSamplerDsl.parentBased(root = newSamplerDsl.alwaysOn())

    private fun setSampler(sampler: Sampler) {
        val otelJavaSampler = when (sampler) {
            is SamplerAdapter -> sampler.impl
            else -> OtelJavaSamplerAdapter(sampler)
        }
        builder.setSampler(otelJavaSampler)
    }

    override fun tracerConfigurator(configurator: TracerConfigurator) {
        tracerConfigurator = configurator
    }

    private fun applyTracerConfigurator(configurator: TracerConfigurator) {
        val scopeConfigurator = OtelJavaScopeConfigurator<OtelJavaTracerConfig> { javaScope ->
            val scope = javaScope.toOtelKotlinInstrumentationScopeInfo()
            when (configurator.tracerConfig(scope).enabled) {
                true -> OtelJavaTracerConfig.enabled()
                false -> OtelJavaTracerConfig.disabled()
            }
        }
        OtelJavaSdkTracerProviderUtil.setTracerConfigurator(builder, scopeConfigurator)
    }

    fun build(
        clock: Clock,
        idGenerator: IdGenerator,
        baseResource: Resource = ResourceAdapter(OtelJavaResource.builder().build()),
        spanLimits: SpanLimitsBehavior,
        contextFactory: ContextFactory = CompatContextFactory(),
    ): TracerProvider {
        builder.setIdGenerator(
            when (idGenerator) {
                is OtelJavaIdGenerator -> idGenerator
                else -> OtelJavaIdGeneratorAdapter(idGenerator)
            }
        )
        spanLimitsConfig.attributeCountLimit = spanLimits.attributeCountLimit
        spanLimitsConfig.attributeValueLengthLimit = spanLimits.attributeValueLengthLimit
        spanLimitsConfig.linkCountLimit = spanLimits.linkCountLimit
        spanLimitsConfig.eventCountLimit = spanLimits.eventCountLimit
        spanLimitsConfig.attributeCountPerEventLimit = spanLimits.attributeCountPerEventLimit
        spanLimitsConfig.attributeCountPerLinkLimit = spanLimits.attributeCountPerLinkLimit
        builder.setSpanLimits(spanLimitsConfig.build())
        tracerConfigurator?.let(::applyTracerConfigurator)
        val resource = ResourceAdapter(
            OtelJavaResource.create(resourceAttrs.otelJavaAttributes(), resourceSchemaUrl)
        )
        val merged = baseResource.merge(resource)
        if (merged.attributes.isNotEmpty() || merged.schemaUrl != null) {
            val attrs = attrsFromMap(merged.attributes)
            builder.setResource(OtelJavaResource.create(attrs, merged.schemaUrl))
        }
        builder.setClock(clock.toOtelJavaClock())
        sampler?.let(::setSampler)
        return TracerProviderAdapter(builder.build(), spanLimitsConfig, contextFactory, sdkErrorHandler)
    }

    fun toBehavior(): TracerProviderBehavior =
        TracerProviderBehavior(
            spanLimits = spanLimitsDsl.toBehavior()
        )
}
