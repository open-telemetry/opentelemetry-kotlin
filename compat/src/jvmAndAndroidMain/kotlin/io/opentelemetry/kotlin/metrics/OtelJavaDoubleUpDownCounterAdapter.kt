package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounter
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.attributes.setTypedAttributes

internal class OtelJavaDoubleUpDownCounterAdapter(
    private val impl: DoubleUpDownCounter,
) : OtelJavaDoubleUpDownCounter {
    override fun isEnabled(): Boolean = impl.enabled()

    override fun add(value: Double) {
        impl.add(value)
    }

    override fun add(value: Double, attributes: OtelJavaAttributes) {
        if (attributes.isEmpty) {
            impl.add(value)
        } else {
            impl.add(value) { setTypedAttributes(attributes.convertToMap()) }
        }
    }

    override fun add(value: Double, attributes: OtelJavaAttributes, context: OtelJavaContext) {
        add(value, attributes)
    }
}
