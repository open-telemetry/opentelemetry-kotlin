package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanLimits
import io.opentelemetry.kotlin.behavior.limitOrUnset

@ExperimentalApi
internal class CompatSpanLimitsConfig : SpanLimitsConfigDsl {

    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null
    override var linkCountLimit: Int? = null
    override var eventCountLimit: Int? = null
    override var attributeCountPerEventLimit: Int? = null
    override var attributeCountPerLinkLimit: Int? = null

    /**
     * The Java SDK cannot report dropped links, events, or attributes back to us, so the adapters
     * enforce those three limits themselves and need the default filled in here.
     */
    val effectiveAttributeCountLimit: Int
        get() = limitOrUnset(attributeCountLimit) ?: DEFAULT_ATTR_LIMIT

    val effectiveLinkCountLimit: Int
        get() = limitOrUnset(linkCountLimit) ?: DEFAULT_LINK_LIMIT

    val effectiveEventCountLimit: Int
        get() = limitOrUnset(eventCountLimit) ?: DEFAULT_EVENT_LIMIT

    /**
     * Only the limits that were configured are set, so anything left unset falls back to the Java
     * SDK's own default.
     */
    fun build(): OtelJavaSpanLimits = OtelJavaSpanLimits.builder().apply {
        limitOrUnset(attributeCountLimit)?.let(::setMaxNumberOfAttributes)
        limitOrUnset(attributeValueLengthLimit)?.let(::setMaxAttributeValueLength)
        limitOrUnset(linkCountLimit)?.let(::setMaxNumberOfLinks)
        limitOrUnset(eventCountLimit)?.let(::setMaxNumberOfEvents)
        limitOrUnset(attributeCountPerEventLimit)?.let(::setMaxNumberOfAttributesPerEvent)
        limitOrUnset(attributeCountPerLinkLimit)?.let(::setMaxNumberOfAttributesPerLink)
    }.build()
}
