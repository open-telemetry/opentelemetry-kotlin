package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounter
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel

@ExperimentalApi
internal class DoubleUpDownCounterAdapter(
    private val impl: OtelJavaDoubleUpDownCounter,
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : DoubleUpDownCounter {
    override fun enabled(): Boolean = impl.isEnabled()

    override fun add(value: Double, attributes: AttributesMutator.() -> Unit) {
        val container = CompatAttributesModel()
        attributes(container)
        impl.add(value, container.otelJavaAttributes())
    }
}
