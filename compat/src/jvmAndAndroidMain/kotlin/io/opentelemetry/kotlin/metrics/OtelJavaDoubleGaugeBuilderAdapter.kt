package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaDoubleGauge
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleGaugeBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleGauge
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import java.util.function.Consumer

internal class OtelJavaDoubleGaugeBuilderAdapter(
    private val meter: Meter,
    private val name: String,
    private val javaMeter: OtelJavaMeter,
    private var unit: String? = null,
    private var description: String? = null,
) : OtelJavaDoubleGaugeBuilder {
    override fun setDescription(description: String): OtelJavaDoubleGaugeBuilder {
        this.description = description
        return this
    }

    override fun setUnit(unit: String): OtelJavaDoubleGaugeBuilder {
        this.unit = unit
        return this
    }

    override fun ofLongs(): io.opentelemetry.kotlin.aliases.OtelJavaLongGaugeBuilder =
        javaMeter.gaugeBuilder(name).ofLongs().also { builder ->
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
        }

    override fun build(): OtelJavaDoubleGauge =
        OtelJavaDoubleGaugeAdapter(meter.createDoubleGauge(name, unit, description))

    override fun buildWithCallback(
        callback: Consumer<OtelJavaObservableDoubleMeasurement>,
    ): OtelJavaObservableDoubleGauge =
        javaMeter.gaugeBuilder(name).also { builder ->
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
        }.buildWithCallback(callback)
}
