package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT

internal class LogLimitsConfigImpl : LogLimitsConfigDsl {
    override var attributeCountLimit: Int = DEFAULT_ATTRIBUTE_LIMIT
    override var attributeValueLengthLimit: Int = Int.MAX_VALUE
}
