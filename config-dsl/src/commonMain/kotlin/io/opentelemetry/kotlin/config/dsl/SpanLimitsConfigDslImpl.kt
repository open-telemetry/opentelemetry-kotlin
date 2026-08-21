package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.init.SpanLimitsConfigDsl

/**
 * Captures the span limits configured programmatically, and maps them onto a behavior.
 */
@ExperimentalApi
class SpanLimitsConfigDslImpl : SpanLimitsConfigDsl, BehaviorSupplier<SpanLimitsBehavior> {

    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null
    override var linkCountLimit: Int? = null
    override var eventCountLimit: Int? = null
    override var attributeCountPerEventLimit: Int? = null
    override var attributeCountPerLinkLimit: Int? = null

    override fun toBehavior(): SpanLimitsBehavior = SpanLimitsBehavior(
        attributeCountLimit = limitOrUnset(attributeCountLimit),
        attributeValueLengthLimit = limitOrUnset(attributeValueLengthLimit),
        linkCountLimit = limitOrUnset(linkCountLimit),
        eventCountLimit = limitOrUnset(eventCountLimit),
        attributeCountPerEventLimit = limitOrUnset(attributeCountPerEventLimit),
        attributeCountPerLinkLimit = limitOrUnset(attributeCountPerLinkLimit),
    )
}
