package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits

@ExperimentalApi
internal class CompatLogLimitsConfig : LogLimitsConfigDsl {

    private val builder = OtelJavaLogLimits.builder()

    override var attributeCountLimit: Int = 0
        set(value) {
            field = value
            builder.setMaxNumberOfAttributes(value)
        }

    override var attributeValueLengthLimit: Int = 0
        set(value) {
            field = value
            builder.setMaxAttributeValueLength(value)
        }

    fun build(): OtelJavaLogLimits = builder.build()
}
