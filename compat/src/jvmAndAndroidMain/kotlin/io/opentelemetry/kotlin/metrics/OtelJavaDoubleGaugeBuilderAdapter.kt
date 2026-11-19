package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaDoubleGauge
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleGaugeBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLongGaugeBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleGauge
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import java.util.function.Consumer

internal class OtelJavaDoubleGaugeBuilderAdapter(
    private val meter: Meter,
    private val name: String,
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

    override fun ofLongs(): OtelJavaLongGaugeBuilder =
        OtelJavaLongGaugeBuilderAdapter(meter, name, unit, description)

    override fun build(): OtelJavaDoubleGauge =
        OtelJavaDoubleGaugeAdapter(meter.createDoubleGauge(name, unit, description))

    override fun buildObserver(): OtelJavaObservableDoubleMeasurement =
        OtelJavaObservableDoubleMeasurementAdapter(
            meter.createObservableDoubleGauge(name, unit, description).measurement
        )

    override fun buildWithCallback(
        callback: Consumer<OtelJavaObservableDoubleMeasurement>,
    ): OtelJavaObservableDoubleGauge {
        val measurementCallback: (ObservableDoubleMeasurement) -> Unit = { measurement ->
            callback.accept(OtelJavaObservableDoubleMeasurementAdapter(measurement))
        }
        val gauge = meter.createObservableDoubleGauge(name, unit, description, measurementCallback)
        return object : OtelJavaObservableDoubleGauge {
            override fun close() = gauge.close()
        }
    }
}
