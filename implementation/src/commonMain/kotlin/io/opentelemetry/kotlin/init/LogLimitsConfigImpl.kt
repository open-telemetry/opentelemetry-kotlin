package io.opentelemetry.kotlin.init

internal class LogLimitsConfigImpl : LogLimitsConfigDsl {
    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null
}
