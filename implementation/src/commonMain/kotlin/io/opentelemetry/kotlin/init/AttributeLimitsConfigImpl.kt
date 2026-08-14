package io.opentelemetry.kotlin.init

internal class AttributeLimitsConfigImpl : AttributeLimitsConfigDsl {
    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null
}
