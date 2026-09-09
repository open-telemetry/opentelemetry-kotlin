package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.dsl.SpanLimitsConfigDslImpl
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.init.config.DEFAULT_EVENT_LIMIT
import io.opentelemetry.kotlin.init.config.DEFAULT_LINK_LIMIT
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.TracerConfigImpl
import io.opentelemetry.kotlin.tracing.TracerConfigurator
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.alwaysOn
import io.opentelemetry.kotlin.tracing.sampling.parentBased

internal class TracerProviderConfigImpl(
    private val clock: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : TracerProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    private var processor: SpanProcessor? = null
    private var samplerAction: SamplerConfigDsl.() -> Sampler = { parentBased(root = alwaysOn()) }
    private val defaultTracerConfig = TracerConfigImpl(true)
    private var tracerConfigurator: TracerConfigurator = TracerConfigurator {
        defaultTracerConfig
    }
    private val spanLimits = SpanLimitsConfigDslImpl()

    override fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit) {
        spanLimits.action()
    }

    override fun export(action: TraceExportConfigDsl.() -> SpanProcessor) {
        if (processor != null) {
            sdkErrorHandler.reportError(
                SdkError.ApiMisuse(
                    api = "TracerProviderConfigDsl.export",
                    message = "export() should only be called once.",
                    severity = SdkErrorSeverity.WARNING,
                )
            )
            return
        }
        processor = TraceExportConfigImpl(clock, sdkErrorHandler).action()
    }

    override fun sampler(action: SamplerConfigDsl.() -> Sampler) {
        samplerAction = action
    }

    override fun tracerConfigurator(configurator: TracerConfigurator) {
        tracerConfigurator = configurator
    }

    fun generateTracingConfig(
        base: Resource,
        globalLimits: AttributeLimitsBehavior,
        spanLimits: SpanLimitsBehavior,
    ): TracingConfig = TracingConfig(
        processor = processor,
        spanLimits = generateSpanLimitsConfig(globalLimits, spanLimits),
        resource = base.merge(resourceConfigImpl.generateResource()),
        sdkErrorHandler = sdkErrorHandler,
        samplerFactory = { spanFactory -> SamplerConfigImpl(spanFactory).samplerAction() },
        tracerConfigurator = tracerConfigurator,
    )

    fun toBehavior(): TracerProviderBehavior =
        TracerProviderBehavior(
            spanLimits = spanLimits.toBehavior()
        )

    private class SamplerConfigImpl(override val spanFactory: SpanFactory) : SamplerConfigDsl

    /**
     * A limit left unset by the span limits falls back to the global attribute limits, then to the
     * default this SDK applies. Only the attribute limits are configurable globally.
     */
    private fun generateSpanLimitsConfig(
        globalLimits: AttributeLimitsBehavior,
        spanLimits: SpanLimitsBehavior
    ): SpanLimitConfig {
        return SpanLimitConfig(
            attributeCountLimit = spanLimits.attributeCountLimit
                ?: globalLimits.attributeCountLimit
                ?: DEFAULT_ATTRIBUTE_LIMIT,
            attributeValueLengthLimit = spanLimits.attributeValueLengthLimit
                ?: globalLimits.attributeValueLengthLimit
                ?: DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT,
            linkCountLimit = spanLimits.linkCountLimit ?: DEFAULT_LINK_LIMIT,
            eventCountLimit = spanLimits.eventCountLimit ?: DEFAULT_EVENT_LIMIT,
            attributeCountPerEventLimit = spanLimits.attributeCountPerEventLimit ?: DEFAULT_ATTRIBUTE_LIMIT,
            attributeCountPerLinkLimit = spanLimits.attributeCountPerLinkLimit ?: DEFAULT_ATTRIBUTE_LIMIT,
        )
    }
}
