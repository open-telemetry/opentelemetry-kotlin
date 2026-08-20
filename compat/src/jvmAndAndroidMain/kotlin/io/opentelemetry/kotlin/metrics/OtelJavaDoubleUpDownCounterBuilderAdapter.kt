package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounter
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleUpDownCounter
import java.util.function.Consumer

internal class OtelJavaDoubleUpDownCounterBuilderAdapter(
    private val meter: Meter,
    private val name: String,
    private var unit: String? = null,
    private var description: String? = null,
) : OtelJavaDoubleUpDownCounterBuilder {

    override fun setDescription(description: String): OtelJavaDoubleUpDownCounterBuilder {
        this.description = description
        return this
    }

    override fun setUnit(unit: String): OtelJavaDoubleUpDownCounterBuilder {
        this.unit = unit
        return this
    }

    override fun build(): OtelJavaDoubleUpDownCounter =
        OtelJavaDoubleUpDownCounterAdapter(meter.createDoubleUpDownCounter(name, unit, description))

    override fun buildWithCallback(
        callback: Consumer<OtelJavaObservableDoubleMeasurement>,
    ): OtelJavaObservableDoubleUpDownCounter =
        OtelJavaMeterProvider.noop().get("").upDownCounterBuilder(name).ofDoubles()
            .buildWithCallback(callback)
}
