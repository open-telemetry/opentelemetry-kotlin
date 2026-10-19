package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel

@ExperimentalApi
internal class ObservableDoubleMeasurementAdapter(
    private val impl: OtelJavaObservableDoubleMeasurement,
) : ObservableDoubleMeasurement {
    override fun record(value: Double) {
        impl.record(value)
    }

    override fun record(
        value: Double,
        attributes: AttributesMutator.() -> Unit,
    ) {
        val container = CompatAttributesModel()
        attributes(container)
        impl.record(value, container.otelJavaAttributes())
    }
}
