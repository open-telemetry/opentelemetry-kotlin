package io.opentelemetry.kotlin.init

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
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.dsl.SpanLimitsConfigDslImpl
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatSpanContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
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
import io.opentelemetry.kotlin.tracing.sampling.OtelJavaSamplerAdapter
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplerAdapter
import io.opentelemetry.kotlin.tracing.sampling.toSampler

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
    private var samplerConfiguredByDsl = false

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
        val processor = TraceExportConfigCompat(clock, sdkErrorHandler).action()
        builder.addSpanProcessor(OtelJavaSpanProcessorAdapter(processor))
    }

    override fun sampler(action: SamplerConfigDsl.() -> Sampler) {
        samplerConfiguredByDsl = true
        setSampler(newSamplerDsl().action())
    }

    internal fun applyResolvedSampler(behavior: SamplerBehavior?) {
        if (samplerConfiguredByDsl || behavior == null) {
            return
        }
        setSampler(newSamplerDsl().toSampler(behavior))
    }

    private fun newSamplerDsl(): SamplerConfigDsl = object : SamplerConfigDsl {
        override val spanFactory = CompatSpanFactory(CompatSpanContextFactory())
    }

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
        globalLimits: AttributeLimitsBehavior,
        spanLimits: SpanLimitsBehavior,
    ): TracerProvider {
        builder.setIdGenerator(
            when (idGenerator) {
                is OtelJavaIdGenerator -> idGenerator
                else -> OtelJavaIdGeneratorAdapter(idGenerator)
            }
        )
        spanLimitsConfig.attributeCountLimit =
            spanLimits.attributeCountLimit ?: globalLimits.attributeCountLimit
        spanLimitsConfig.attributeValueLengthLimit =
            spanLimits.attributeValueLengthLimit ?: globalLimits.attributeValueLengthLimit
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
        builder.setClock(OtelJavaClockWrapper(clock))
        return TracerProviderAdapter(builder.build(), clock, spanLimitsConfig)
    }

    fun toBehavior(): TracerProviderBehavior =
        TracerProviderBehavior(
            spanLimits = spanLimitsDsl.toBehavior()
        )

    private class TraceExportConfigCompat(
        override val clock: Clock,
        override val sdkErrorHandler: SdkErrorHandler,
    ) : TraceExportConfigDsl
}
