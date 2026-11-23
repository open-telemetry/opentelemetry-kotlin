package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleGauge
import io.opentelemetry.kotlin.attributes.setOtelJavaAttributes

internal class OtelJavaDoubleGaugeAdapter(
    private val impl: DoubleGauge,
) : OtelJavaDoubleGauge {
    override fun isEnabled(): Boolean = impl.enabled()

    override fun set(value: Double) {
        impl.record(value)
    }

    override fun set(value: Double, attributes: OtelJavaAttributes) {
        if (attributes.isEmpty) {
            impl.record(value)
        } else {
            impl.record(value) { setOtelJavaAttributes(attributes) }
        }
    }

    override fun set(value: Double, attributes: OtelJavaAttributes, context: OtelJavaContext) {
        set(value, attributes)
    }
}
