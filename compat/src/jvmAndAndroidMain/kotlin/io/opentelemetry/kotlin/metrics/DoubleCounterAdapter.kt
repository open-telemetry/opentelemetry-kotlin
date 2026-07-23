package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaDoubleCounter
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext

internal class DoubleCounterAdapter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
    meter: OtelJavaMeter
): DoubleCounter {

    private val counter: OtelJavaDoubleCounter

    init {
        val builder = meter.counterBuilder(name).ofDoubles()
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
        value: Double,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val model = CompatAttributesModel()
        if (attributes != null) {
            attributes(model)
        }
        counter.add(value, model.otelJavaAttributes())
    }
}