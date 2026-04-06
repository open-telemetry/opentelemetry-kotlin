package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.alwaysOn

internal class TracerProviderConfigImpl(
    private val clock: Clock,
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : TracerProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    private var processor: SpanProcessor? = null
    private var spanLimitsAction: SpanLimitsConfigDsl.() -> Unit = {}
    private var samplerAction: SamplerConfigDsl.() -> Sampler = { alwaysOn() }

    override fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit) {
        spanLimitsAction = action
    }

    override fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        require(processor == null) { "export() should only be called once." }
        processor = TraceExportConfigImpl(clock).action()
    }

    override fun sampler(action: SamplerConfigDsl.() -> Sampler) {
        samplerAction = action
    }

    fun generateTracingConfig(base: Resource, globalLimits: AttributeLimitsConfigImpl? = null): TracingConfig = TracingConfig(
        processor = processor,
        spanLimits = generateSpanLimitsConfig(globalLimits),
        resource = base.merge(resourceConfigImpl.generateResource()),
        samplerFactory = { spanFactory -> SamplerConfigImpl(spanFactory).samplerAction() },
    )

    private class SamplerConfigImpl(override val spanFactory: SpanFactory) : SamplerConfigDsl

    private fun generateSpanLimitsConfig(globalLimits: AttributeLimitsConfigImpl?): SpanLimitConfig {
        val impl = SpanLimitsConfigImpl()
        globalLimits?.let {
            impl.attributeCountLimit = it.attributeCountLimit
            impl.attributeValueLengthLimit = it.attributeValueLengthLimit
        }
        spanLimitsAction(impl)
        return SpanLimitConfig(
            attributeCountLimit = impl.attributeCountLimit,
            attributeValueLengthLimit = impl.attributeValueLengthLimit,
            linkCountLimit = impl.linkCountLimit,
            eventCountLimit = impl.eventCountLimit,
            attributeCountPerEventLimit = impl.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = impl.attributeCountPerLinkLimit,
        )
    }
}
