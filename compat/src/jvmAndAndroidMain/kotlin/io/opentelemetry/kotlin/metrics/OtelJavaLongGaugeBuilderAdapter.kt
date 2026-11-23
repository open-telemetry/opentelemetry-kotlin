package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaLongGauge
import io.opentelemetry.kotlin.aliases.OtelJavaLongGaugeBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongGauge
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import java.util.function.Consumer

internal class OtelJavaLongGaugeBuilderAdapter(
    private val meter: Meter,
    private val name: String,
    private var unit: String? = null,
    private var description: String? = null,
) : OtelJavaLongGaugeBuilder {
    override fun setDescription(description: String): OtelJavaLongGaugeBuilder {
        this.description = description
        return this
    }

    override fun setUnit(unit: String): OtelJavaLongGaugeBuilder {
        this.unit = unit
        return this
    }

    override fun build(): OtelJavaLongGauge =
        OtelJavaLongGaugeAdapter(meter.createLongGauge(name, unit, description))

    override fun buildObserver(): OtelJavaObservableLongMeasurement =
        OtelJavaObservableLongMeasurementAdapter(
            meter.createObservableLongGauge(name, unit, description).measurement
        )

    override fun buildWithCallback(
        callback: Consumer<OtelJavaObservableLongMeasurement>,
    ): OtelJavaObservableLongGauge {
        val measurementCallback: (ObservableLongMeasurement) -> Unit = { measurement ->
            callback.accept(OtelJavaObservableLongMeasurementAdapter(measurement))
        }
        val gauge = meter.createObservableLongGauge(name, unit, description, measurementCallback)
        return object : OtelJavaObservableLongGauge {
            override fun close() = gauge.close()
        }
    }
}
