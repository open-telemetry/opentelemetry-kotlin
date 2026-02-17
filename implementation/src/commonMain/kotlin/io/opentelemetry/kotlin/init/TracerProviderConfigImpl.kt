package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

@OptIn(ExperimentalApi::class)
internal class TracerProviderConfigImpl(
    private val clock: Clock,
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : TracerProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    private val processors: MutableList<SpanProcessor> = mutableListOf()
    private val spanLimitsConfigImpl = SpanLimitsConfigImpl()

    override fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit) {
        spanLimitsConfigImpl.action()
    }

    @Deprecated("Deprecated.", replaceWith = ReplaceWith("export {processor}"))
    override fun addSpanProcessor(processor: SpanProcessor) {
        processors.add(processor)
    }

    override fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        require(processors.isEmpty()) { "export() should only be called once." }
        val processor = TraceExportConfigImpl(clock).action()
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
