package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BehaviorSupplier
import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.behavior.tracing.SamplerBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.TracerProviderBehavior
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.init.ExportConfigDsl
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

/**
 * Captures tracer provider configuration and maps it onto a behavior.
 */
@OptIn(ExperimentalApi::class)
class TracerProviderConfigDslImpl : BehaviorSupplier<TracerProviderBehavior> {

    private var processor: SpanProcessorBehavior? = null
    private var sampler: SamplerBehavior? = null
    private var limits: SpanLimitsBehavior? = null
    private var idGenerator: IdGeneratorBehavior? = null

    @Suppress("UnusedParameter")
    fun export(action: ExportConfigDsl.() -> SpanProcessor) {
        if (processor == null) {
            processor = SpanProcessorBehavior()
        }
    }

    fun processor(action: SpanProcessorConfigDslImpl.() -> Unit) {
        val impl = SpanProcessorConfigDslImpl()
        impl.action()
        processor = impl.toBehavior()
    }

    fun sampler(action: SamplerConfigDslImpl.() -> Unit) {
        val impl = SamplerConfigDslImpl()
        impl.action()
        sampler = impl.toBehavior()
    }

    fun limits(action: SpanLimitsConfigDslImpl.() -> Unit) {
        val impl = SpanLimitsConfigDslImpl()
        impl.action()
        limits = impl.toBehavior()
    }

    fun idGenerator() {
        idGenerator = IdGeneratorBehavior.Random
    }

    override fun toBehavior(): TracerProviderBehavior =
        TracerProviderBehavior(processor = processor, sampler = sampler, spanLimits = limits, idGenerator = idGenerator)
}
