package io.opentelemetry.kotlin.init

internal class CompatAttributeLimitsConfig : AttributeLimitsConfigDsl {
    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null
}
