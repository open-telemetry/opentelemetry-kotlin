package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT

internal class AttributeLimitsConfigImpl : AttributeLimitsConfigDsl {
    override var attributeCountLimit: Int = DEFAULT_ATTRIBUTE_LIMIT
    override var attributeValueLengthLimit: Int = DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
}
