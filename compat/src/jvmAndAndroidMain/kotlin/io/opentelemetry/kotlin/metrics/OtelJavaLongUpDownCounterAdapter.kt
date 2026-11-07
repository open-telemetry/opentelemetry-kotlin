package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounter
import io.opentelemetry.kotlin.attributes.setOtelJavaAttributes

internal class OtelJavaLongUpDownCounterAdapter(
    private val impl: LongUpDownCounter,
) : OtelJavaLongUpDownCounter {
    override fun isEnabled(): Boolean = impl.enabled()

    override fun add(value: Long) {
        impl.add(value)
    }

    override fun add(value: Long, attributes: OtelJavaAttributes) {
        if (attributes.isEmpty) {
            impl.add(value)
        } else {
            impl.add(value) { setOtelJavaAttributes(attributes) }
        }
    }

    override fun add(value: Long, attributes: OtelJavaAttributes, context: OtelJavaContext) {
        add(value, attributes)
    }
}
