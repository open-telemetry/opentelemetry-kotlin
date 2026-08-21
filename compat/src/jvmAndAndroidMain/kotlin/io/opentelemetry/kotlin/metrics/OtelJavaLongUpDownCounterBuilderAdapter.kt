package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounter
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongUpDownCounter
import java.util.function.Consumer

internal class OtelJavaLongUpDownCounterBuilderAdapter(
    private val meter: Meter,
    private val name: String,
    private var unit: String? = null,
    private var description: String? = null,
) : OtelJavaLongUpDownCounterBuilder {

    override fun setDescription(description: String): OtelJavaLongUpDownCounterBuilder {
        this.description = description
        return this
    }

    override fun setUnit(unit: String): OtelJavaLongUpDownCounterBuilder {
        this.unit = unit
        return this
    }

    override fun ofDoubles(): OtelJavaDoubleUpDownCounterBuilder =
        OtelJavaMeterProvider.noop().get("").upDownCounterBuilder(name).ofDoubles()

    override fun build(): OtelJavaLongUpDownCounter =
        OtelJavaLongUpDownCounterAdapter(meter.createLongUpDownCounter(name, unit, description))

    override fun buildWithCallback(
        callback: Consumer<OtelJavaObservableLongMeasurement>,
    ): OtelJavaObservableLongUpDownCounter =
        OtelJavaMeterProvider.noop().get("").upDownCounterBuilder(name).buildWithCallback(callback)
}
