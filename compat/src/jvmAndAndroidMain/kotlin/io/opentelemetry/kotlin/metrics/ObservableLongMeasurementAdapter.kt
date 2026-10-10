package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel

@ExperimentalApi
internal class ObservableLongMeasurementAdapter(
    private val impl: OtelJavaObservableLongMeasurement,
) : ObservableLongMeasurement {
    override fun record(value: Long) {
        impl.record(value)
    }

    override fun record(
        value: Long,
        attributes: AttributesMutator.() -> Unit,
    ) {
        val container = CompatAttributesModel()
        attributes(container)
        impl.record(value, container.otelJavaAttributes())
    }
}
