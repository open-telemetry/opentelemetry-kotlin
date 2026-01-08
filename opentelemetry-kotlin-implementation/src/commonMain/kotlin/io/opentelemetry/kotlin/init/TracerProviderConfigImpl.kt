package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

@OptIn(ExperimentalApi::class)
internal class TracerProviderConfigImpl(
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : TracerProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    private val processors: MutableList<SpanProcessor> = mutableListOf()
    private val spanLimitsConfigImpl = SpanLimitsConfigImpl()

    override fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit) {
        spanLimitsConfigImpl.action()
    }

    override fun addSpanProcessor(processor: SpanProcessor) {
        processors.add(processor)
    }

    fun generateTracingConfig(): TracingConfig = TracingConfig(
        processors = processors.toList(),
        spanLimits = generateSpanLimitsConfig(),
        resource = resourceConfigImpl.generateResource(),
    )

    private fun generateSpanLimitsConfig(): SpanLimitConfig = SpanLimitConfig(
        attributeCountLimit = spanLimitsConfigImpl.attributeCountLimit,
        linkCountLimit = spanLimitsConfigImpl.linkCountLimit,
        eventCountLimit = spanLimitsConfigImpl.eventCountLimit,
        attributeCountPerEventLimit = spanLimitsConfigImpl.attributeCountPerEventLimit,
        attributeCountPerLinkLimit = spanLimitsConfigImpl.attributeCountPerLinkLimit
    )
}
