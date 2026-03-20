package io.opentelemetry.kotlin.init

internal class CompatAttributeLimitsConfig : AttributeLimitsConfigDsl {

    internal var attributeCountLimitSet = false
    internal var attributeValueLengthLimitSet = false

    override var attributeCountLimit: Int = 0
        set(value) {
            field = value
            attributeCountLimitSet = true
        }

    override var attributeValueLengthLimit: Int = 0
        set(value) {
            field = value
            attributeValueLengthLimitSet = true
        }
}
