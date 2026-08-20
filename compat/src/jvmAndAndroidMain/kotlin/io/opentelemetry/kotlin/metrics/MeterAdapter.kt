package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaMeter

@ExperimentalApi
internal class MeterAdapter(
    private val impl: OtelJavaMeter,
) : Meter {
    override fun createLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): LongUpDownCounter {
        val builder = impl.upDownCounterBuilder(name)
        unit?.let(builder::setUnit)
        description?.let(builder::setDescription)
        return LongUpDownCounterAdapter(builder.build(), name, unit, description)
    }

    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter {
        val builder = impl.upDownCounterBuilder(name).ofDoubles()
        unit?.let(builder::setUnit)
        description?.let(builder::setDescription)
        return DoubleUpDownCounterAdapter(builder.build(), name, unit, description)
    }
}
