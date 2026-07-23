package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaDoubleCounter
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext

internal class FloatCounterAdapter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
    meter: OtelJavaMeter
): FloatCounter {

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
        value: Float,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val model = CompatAttributesModel()
        if (attributes != null) {
            attributes(model)
        }
        counter.add(value.toDouble(), model.otelJavaAttributes())
    }
}