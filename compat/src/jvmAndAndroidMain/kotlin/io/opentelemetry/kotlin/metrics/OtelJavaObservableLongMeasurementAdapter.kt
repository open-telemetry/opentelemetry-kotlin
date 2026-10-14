package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.attributes.setTypedAttributes

internal class OtelJavaObservableLongMeasurementAdapter(
    private val impl: ObservableLongMeasurement,
) : OtelJavaObservableLongMeasurement {
    override fun record(value: Long) {
        impl.record(value)
    }

    override fun record(value: Long, attributes: OtelJavaAttributes) {
        if (attributes.isEmpty) {
            impl.record(value)
        } else {
            impl.record(value) { setTypedAttributes(attributes.convertToMap()) }
        }
    }
}
