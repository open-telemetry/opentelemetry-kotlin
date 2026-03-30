package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits

@ExperimentalApi
internal class CompatLogLimitsConfig : LogLimitsConfigDsl {

    private val builder = OtelJavaLogLimits.builder()

    override var attributeCountLimit: Int = DEFAULT_ATTR_LIMIT
        set(value) {
            field = value
            builder.setMaxNumberOfAttributes(value)
        }

    override var attributeValueLengthLimit: Int = DEFAULT_ATTR_VALUE_LENGTH_LIMIT
        set(value) {
            field = value
            builder.setMaxAttributeValueLength(value)
        }

    fun build(): OtelJavaLogLimits = builder.build()
}
