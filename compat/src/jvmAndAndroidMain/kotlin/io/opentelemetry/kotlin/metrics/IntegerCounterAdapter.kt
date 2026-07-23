package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaLongCounter
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext

internal class IntegerCounterAdapter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
    meter: OtelJavaMeter
): IntegerCounter {

    private val counter: OtelJavaLongCounter

    init {
        val builder = meter.counterBuilder(name)
        if (unit != null) {
            builder.setUnit(unit)
        }
        if (description != null) {
            builder.setDescription(description)
        }
        counter = builder.build()
    }


    override fun enabled(): Boolean {
        return counter.isEnabled()
    }

    override fun add(
        value: UInt,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val model = CompatAttributesModel()
        if (attributes != null) {
            attributes(model)
        }
        counter.add(value.toLong(), model.otelJavaAttributes())
    }
}