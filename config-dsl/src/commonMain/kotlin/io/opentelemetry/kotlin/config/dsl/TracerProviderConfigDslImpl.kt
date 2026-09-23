package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

/**
 * Captures tracer provider configuration and maps it onto a behavior.
 */
@ExperimentalApi
class TracerProviderConfigDslImpl : BehaviorSupplier<TracerProviderBehavior> {

    private var exporter: SpanExporterBehavior? = null
    private var sampler: SamplerBehavior? = null

    @Suppress("UnusedParameter")
    fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        exporter = SpanExporterBehavior.Console
    }

    fun sampler(action: SamplerConfigDslImpl.() -> Unit) {
        val impl = SamplerConfigDslImpl()
        impl.action()
        sampler = impl.toBehavior()
    }

    override fun toBehavior(): TracerProviderBehavior =
        TracerProviderBehavior(
            processor = exporter?.let { SpanProcessorBehavior.Simple(exporter = it) },
            sampler = sampler,
        )
}
