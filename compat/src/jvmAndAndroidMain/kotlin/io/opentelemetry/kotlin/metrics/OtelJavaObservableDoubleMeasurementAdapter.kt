package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.attributes.setTypedAttributes

internal class OtelJavaObservableDoubleMeasurementAdapter(
    private val impl: ObservableDoubleMeasurement,
) : OtelJavaObservableDoubleMeasurement {
    override fun record(value: Double) {
        impl.record(value)
    }

    override fun record(value: Double, attributes: OtelJavaAttributes) {
        if (attributes.isEmpty) {
            impl.record(value)
        } else {
            impl.record(value) { setTypedAttributes(attributes.convertToMap()) }
        }
    }
}
