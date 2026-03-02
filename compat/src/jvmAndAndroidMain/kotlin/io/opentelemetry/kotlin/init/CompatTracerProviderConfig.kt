package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProviderBuilder
import io.opentelemetry.kotlin.attributes.CompatMutableAttributeContainer
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.tracing.TracerProvider
import io.opentelemetry.kotlin.tracing.TracerProviderAdapter
import io.opentelemetry.kotlin.tracing.export.OtelJavaSpanProcessorAdapter
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

@ExperimentalApi
internal class CompatTracerProviderConfig(
    private val clock: Clock,
    sdkFactory: SdkFactory,
) : TracerProviderConfigDsl {

    private val builder: OtelJavaSdkTracerProviderBuilder = OtelJavaSdkTracerProvider.builder()
    private val spanLimitsConfig = CompatSpanLimitsConfig()

    init {
        val idGenerator = sdkFactory.tracingIdFactory
        if (idGenerator is OtelJavaIdGenerator) {
            builder.setIdGenerator(idGenerator)
        }
    }

    override fun resource(schemaUrl: String?, attributes: MutableAttributeContainer.() -> Unit) {
        val attrs = CompatMutableAttributeContainer().apply(attributes).otelJavaAttributes()
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

    @Deprecated("Deprecated.", replaceWith = ReplaceWith("export {processor}"))
    override fun addSpanProcessor(processor: SpanProcessor) {
        builder.addSpanProcessor(OtelJavaSpanProcessorAdapter(processor))
    }

    override fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        val processor = TraceExportConfigCompat(clock).action()
        @Suppress("DEPRECATION")
        addSpanProcessor(processor)
    }

    fun build(clock: Clock): TracerProvider {
        builder.setClock(OtelJavaClockWrapper(clock))
        return TracerProviderAdapter(builder.build(), clock, spanLimitsConfig)
    }

    private class TraceExportConfigCompat(override val clock: Clock) : TraceExportConfigDsl
}
