package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset

/**
 * Limits on span data capture. A limit that is unset or invalid falls back to the default this SDK
 * applies.
 */
@ThreadSafe
internal class SpanLimitConfig(
    attributeCountLimit: Int? = null,
    attributeValueLengthLimit: Int? = null,
    linkCountLimit: Int? = null,
    eventCountLimit: Int? = null,
    attributeCountPerEventLimit: Int? = null,
    attributeCountPerLinkLimit: Int? = null,
) {

    /**
     * The global attribute limits have already been folded into [behavior] by the behavior resolver.
     */
    constructor(behavior: SpanLimitsBehavior) : this(
        attributeCountLimit = behavior.attributeCountLimit,
        attributeValueLengthLimit = behavior.attributeValueLengthLimit,
        linkCountLimit = behavior.linkCountLimit,
        eventCountLimit = behavior.eventCountLimit,
        attributeCountPerEventLimit = behavior.attributeCountPerEventLimit,
        attributeCountPerLinkLimit = behavior.attributeCountPerLinkLimit,
    )

    /**
     * Max attribute count.
     */
    val attributeCountLimit: Int = limitOrUnset(attributeCountLimit) ?: DEFAULT_ATTRIBUTE_LIMIT

    /**
     * Max attribute value length.
     */
    val attributeValueLengthLimit: Int = limitOrUnset(attributeValueLengthLimit) ?: DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT

    /**
     * Max link count.
     */
    val linkCountLimit: Int = limitOrUnset(linkCountLimit) ?: DEFAULT_LINK_LIMIT

    /**
     * Max event count.
     */
    val eventCountLimit: Int = limitOrUnset(eventCountLimit) ?: DEFAULT_EVENT_LIMIT

    /**
     * Max attributes per event.
     */
    val attributeCountPerEventLimit: Int = limitOrUnset(attributeCountPerEventLimit) ?: DEFAULT_ATTRIBUTE_LIMIT

    /**
     * Max attributes per link.
     */
    val attributeCountPerLinkLimit: Int = limitOrUnset(attributeCountPerLinkLimit) ?: DEFAULT_ATTRIBUTE_LIMIT
}

internal const val DEFAULT_LINK_LIMIT: Int = 128
internal const val DEFAULT_EVENT_LIMIT: Int = 128
