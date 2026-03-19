package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.alwaysOn

internal class TracerProviderConfigImpl(
    private val clock: Clock,
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : TracerProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    private val processors: MutableList<SpanProcessor> = mutableListOf()
    private val spanLimitsConfigImpl = SpanLimitsConfigImpl()
    private var samplerAction: SamplerConfigDsl.() -> Sampler = { alwaysOn() }

    override fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit) {
        spanLimitsConfigImpl.action()
    }

    override fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        require(processors.isEmpty()) { "export() should only be called once." }
        val processor = TraceExportConfigImpl(clock).action()
        processors.add(processor)
    }

    override fun sampler(action: SamplerConfigDsl.() -> Sampler) {
        samplerAction = action
    }

    fun generateTracingConfig(): TracingConfig = TracingConfig(
        processors = processors.toList(),
        spanLimits = generateSpanLimitsConfig(),
        resource = resourceConfigImpl.generateResource(),
        samplerFactory = { spanFactory -> SamplerConfigImpl(spanFactory).samplerAction() },
    )

    private class SamplerConfigImpl(override val spanFactory: SpanFactory) : SamplerConfigDsl

    private fun generateSpanLimitsConfig(): SpanLimitConfig = SpanLimitConfig(
        attributeCountLimit = spanLimitsConfigImpl.attributeCountLimit,
        linkCountLimit = spanLimitsConfigImpl.linkCountLimit,
        eventCountLimit = spanLimitsConfigImpl.eventCountLimit,
        attributeCountPerEventLimit = spanLimitsConfigImpl.attributeCountPerEventLimit,
        attributeCountPerLinkLimit = spanLimitsConfigImpl.attributeCountPerLinkLimit
    )
}
