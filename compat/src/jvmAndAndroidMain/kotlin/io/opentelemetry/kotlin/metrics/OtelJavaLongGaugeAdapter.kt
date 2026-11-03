package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLongGauge
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.attributes.setTypedAttributes

internal class OtelJavaLongGaugeAdapter(
    private val impl: LongGauge,
) : OtelJavaLongGauge {
    override fun isEnabled(): Boolean = impl.enabled()

    override fun set(value: Long) {
        impl.record(value)
    }

    override fun set(value: Long, attributes: OtelJavaAttributes) {
        if (attributes.isEmpty) {
            impl.record(value)
        } else {
            impl.record(value) { setTypedAttributes(attributes.convertToMap()) }
        }
    }

    override fun set(value: Long, attributes: OtelJavaAttributes, context: OtelJavaContext) {
        set(value, attributes)
    }
}
