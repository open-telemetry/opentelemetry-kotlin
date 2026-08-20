package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounter
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel

@ExperimentalApi
internal class LongUpDownCounterAdapter(
    private val impl: OtelJavaLongUpDownCounter,
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : LongUpDownCounter {
    override fun enabled(): Boolean = impl.isEnabled()

    override fun add(value: Long) {
        impl.add(value)
    }

    override fun add(value: Long, attributes: AttributesMutator.() -> Unit) {
        val container = CompatAttributesModel()
        attributes(container)
        impl.add(value, container.otelJavaAttributes())
    }
}
