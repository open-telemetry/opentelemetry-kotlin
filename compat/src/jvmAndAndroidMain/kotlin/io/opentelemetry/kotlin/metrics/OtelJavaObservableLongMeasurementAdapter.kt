package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.attributes.setOtelJavaAttributes

internal class OtelJavaObservableLongMeasurementAdapter(
    internal val impl: ObservableLongMeasurement,
) : OtelJavaObservableLongMeasurement {
    override fun record(value: Long) {
        impl.record(value)
    }

    override fun record(value: Long, attributes: OtelJavaAttributes) {
        if (attributes.isEmpty) {
            impl.record(value)
        } else {
            impl.record(value) { setOtelJavaAttributes(attributes) }
        }
    }
}
