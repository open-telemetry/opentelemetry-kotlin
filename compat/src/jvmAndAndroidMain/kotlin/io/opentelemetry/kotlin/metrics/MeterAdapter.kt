package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaMeter

@ExperimentalApi
internal class MeterAdapter(
    private val impl: OtelJavaMeter,
) : Meter {

    override fun createLongCounter(
        name: String,
        description: String?,
        unit: String?
    ): LongCounter {
        return LongCounterAdapter(name, description, unit, impl)
    }
}
