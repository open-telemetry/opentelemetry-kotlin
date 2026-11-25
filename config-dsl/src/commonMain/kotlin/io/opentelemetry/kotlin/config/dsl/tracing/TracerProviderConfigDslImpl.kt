package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BehaviorSupplier
import io.opentelemetry.kotlin.behavior.tracing.SamplerBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.TracerProviderBehavior

/**
 * Captures tracer provider configuration and maps it onto a behavior.
 */
@OptIn(ExperimentalApi::class)
class TracerProviderConfigDslImpl : BehaviorSupplier<TracerProviderBehavior> {

    private var processors: MutableList<SpanProcessorBehavior> = mutableListOf()
    private var sampler: SamplerBehavior? = null

    fun processor(action: SpanProcessorConfigDslImpl.() -> Unit) {
        val impl = SpanProcessorConfigDslImpl()
        impl.action()
        processors += impl.toBehavior()
    }

    fun sampler(action: SamplerConfigDslImpl.() -> Unit) {
        val impl = SamplerConfigDslImpl()
        impl.action()
        sampler = impl.toBehavior()
    }

    override fun toBehavior(): TracerProviderBehavior {
        return TracerProviderBehavior(processors = processors, sampler = sampler)
    }
}
