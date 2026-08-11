package io.opentelemetry.kotlin.config.model

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Limits on span data capture. A `null` limit is left to whatever default the SDK being configured
 * already applies.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-limits
 */
@ExperimentalApi
data class SpanLimitsConfigModel(

    /**
     * Maximum number of attributes that may be recorded on a span.
     */
    val attributeCountLimit: Int? = null,

    /**
     * Maximum length of a recorded attribute value.
     */
    val attributeValueLengthLimit: Int? = null,

    /**
     * Maximum number of links that may be recorded on a span.
     */
    val linkCountLimit: Int? = null,

    /**
     * Maximum number of events that may be recorded on a span.
     */
    val eventCountLimit: Int? = null,

    /**
     * Maximum number of attributes that may be recorded on a single event.
     */
    val attributeCountPerEventLimit: Int? = null,

    /**
     * Maximum number of attributes that may be recorded on a single link.
     */
    val attributeCountPerLinkLimit: Int? = null,
) : ConfigModel<SpanLimitsConfigModel> {

    override fun mergeWith(higher: SpanLimitsConfigModel): SpanLimitsConfigModel = copy(
        attributeCountLimit = higher.attributeCountLimit ?: attributeCountLimit,
        attributeValueLengthLimit = higher.attributeValueLengthLimit ?: attributeValueLengthLimit,
        linkCountLimit = higher.linkCountLimit ?: linkCountLimit,
        eventCountLimit = higher.eventCountLimit ?: eventCountLimit,
        attributeCountPerEventLimit = higher.attributeCountPerEventLimit ?: attributeCountPerEventLimit,
        attributeCountPerLinkLimit = higher.attributeCountPerLinkLimit ?: attributeCountPerLinkLimit,
    )
}
