package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProviderBuilder
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.tracing.TracerProvider
import io.opentelemetry.kotlin.tracing.TracerProviderAdapter
import io.opentelemetry.kotlin.tracing.export.OtelJavaSpanProcessorAdapter
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.BuiltInSampler
import io.opentelemetry.kotlin.tracing.sampling.OtelJavaSamplerAdapter
import io.opentelemetry.kotlin.tracing.sampling.Sampler

@ExperimentalApi
internal class CompatTracerProviderConfig(
    private val clock: Clock,
    idGenerator: IdGenerator,
) : TracerProviderConfigDsl {

    private val builder: OtelJavaSdkTracerProviderBuilder = OtelJavaSdkTracerProvider.builder()
    private val spanLimitsConfig = CompatSpanLimitsConfig()

    private val resourceAttrs = CompatAttributesModel()
    private var resourceSchemaUrl: String? = null

    init {
        if (idGenerator is OtelJavaIdGenerator) {
            builder.setIdGenerator(idGenerator)
        }
    }

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        resourceSchemaUrl = schemaUrl
        resourceAttrs.apply(attributes)
    }

    override fun resource(map: Map<String, Any>) {
        resourceAttrs.apply { setAttributes(map) }
    }

    override fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit) {
        builder.setSpanLimits(spanLimitsConfig.apply(action).build())
    }

    override fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        val processor = TraceExportConfigCompat(clock).action()
        builder.addSpanProcessor(OtelJavaSpanProcessorAdapter(processor))
    }

    override fun sampler(builtin: BuiltInSampler) {
        builder.setSampler(
            when (builtin) {
                BuiltInSampler.ALWAYS_ON -> OtelJavaSampler.alwaysOn()
                BuiltInSampler.ALWAYS_OFF -> OtelJavaSampler.alwaysOff()
            }
        )
    }

    override fun sampler(factory: () -> Sampler) {
        builder.setSampler(OtelJavaSamplerAdapter(factory()))
    }

    fun build(clock: Clock, baseResource: Resource = ResourceAdapter(OtelJavaResource.builder().build())): TracerProvider {
        val resource = ResourceAdapter(
            OtelJavaResource.create(resourceAttrs.otelJavaAttributes(), resourceSchemaUrl)
        )
        val merged = baseResource.merge(resource)
        if (merged.attributes.isNotEmpty() || merged.schemaUrl != null) {
            val attrs = CompatAttributesModel().apply { setAttributes(merged.attributes) }.otelJavaAttributes()
            builder.setResource(OtelJavaResource.create(attrs, merged.schemaUrl))
        }
        builder.setClock(OtelJavaClockWrapper(clock))
        return TracerProviderAdapter(builder.build(), clock, spanLimitsConfig)
    }

    private class TraceExportConfigCompat(override val clock: Clock) : TraceExportConfigDsl
}
