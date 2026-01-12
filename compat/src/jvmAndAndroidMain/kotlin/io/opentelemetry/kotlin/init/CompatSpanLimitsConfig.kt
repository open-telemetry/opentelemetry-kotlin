package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanLimits

@ExperimentalApi
internal class CompatSpanLimitsConfig : SpanLimitsConfigDsl {

    private val builder = OtelJavaSpanLimits.builder()

    override var attributeCountLimit: Int = 0
        set(value) {
            field = value
            builder.setMaxNumberOfAttributes(value)
        }

    override var linkCountLimit: Int = 0
        set(value) {
            field = value
            builder.setMaxNumberOfLinks(value)
        }

    override var eventCountLimit: Int = 0
        set(value) {
            field = value
            builder.setMaxNumberOfEvents(value)
        }

    override var attributeCountPerEventLimit: Int = 0
        set(value) {
            field = value
            builder.setMaxNumberOfAttributesPerEvent(value)
        }

    override var attributeCountPerLinkLimit: Int = 0
        set(value) {
            field = value
            builder.setMaxNumberOfAttributesPerLink(value)
        }

    fun build(): OtelJavaSpanLimits = builder.build()
}
