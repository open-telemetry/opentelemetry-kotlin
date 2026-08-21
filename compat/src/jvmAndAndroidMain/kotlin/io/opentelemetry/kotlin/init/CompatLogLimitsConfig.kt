package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits

@ExperimentalApi
internal class CompatLogLimitsConfig : LogLimitsConfigDsl {

    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null

    /**
     * Only the limits that were configured are set, so anything left unset falls back to the Java
     * SDK's own default.
     */
    fun build(): OtelJavaLogLimits = OtelJavaLogLimits.builder().apply {
        attributeCountLimit?.let(::setMaxNumberOfAttributes)
        attributeValueLengthLimit?.let(::setMaxAttributeValueLength)
    }.build()
}
