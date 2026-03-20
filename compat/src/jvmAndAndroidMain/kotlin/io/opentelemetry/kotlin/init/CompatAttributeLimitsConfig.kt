package io.opentelemetry.kotlin.init

internal class CompatAttributeLimitsConfig : AttributeLimitsConfigDsl {

    internal var attributeCountLimitSet = false
    internal var attributeValueLengthLimitSet = false

    override var attributeCountLimit: Int = DEFAULT_ATTR_LIMIT
        set(value) {
            field = value
            attributeCountLimitSet = true
        }

    override var attributeValueLengthLimit: Int = DEFAULT_ATTR_VALUE_LENGTH_LIMIT
        set(value) {
            field = value
            attributeValueLengthLimitSet = true
        }
}
