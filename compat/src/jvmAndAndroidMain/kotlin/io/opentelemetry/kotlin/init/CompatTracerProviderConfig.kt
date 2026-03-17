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

    init {
        if (idGenerator is OtelJavaIdGenerator) {
            builder.setIdGenerator(idGenerator)
        }
    }

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        val attrs = CompatAttributesModel().apply(attributes).otelJavaAttributes()
        builder.setResource(OtelJavaResource.create(attrs, schemaUrl))
    }

    override fun resource(map: Map<String, Any>) {
        resource {
            setAttributes(map)
        }
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

    fun build(clock: Clock): TracerProvider {
        builder.setClock(OtelJavaClockWrapper(clock))
        return TracerProviderAdapter(builder.build(), clock, spanLimitsConfig)
    }

    private class TraceExportConfigCompat(override val clock: Clock) : TraceExportConfigDsl
}
