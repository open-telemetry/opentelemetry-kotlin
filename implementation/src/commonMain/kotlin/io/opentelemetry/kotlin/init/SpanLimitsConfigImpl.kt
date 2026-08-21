package io.opentelemetry.kotlin.init

internal class SpanLimitsConfigImpl : SpanLimitsConfigDsl {
    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null
    override var linkCountLimit: Int? = null
    override var eventCountLimit: Int? = null
    override var attributeCountPerEventLimit: Int? = null
    override var attributeCountPerLinkLimit: Int? = null
}
