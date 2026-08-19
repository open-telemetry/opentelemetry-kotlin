package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Global limits on attribute capture, which individual signals may override.
 *
 * https://opentelemetry.io/docs/specs/otel/common/#attribute-limits
 */
@ExperimentalApi
data class AttributeLimitsBehavior(

    /**
     * Maximum number of attributes that may be recorded.
     */
    val attributeCountLimit: Int? = null,

    /**
     * Maximum length of a recorded attribute value.
     */
    val attributeValueLengthLimit: Int? = null,
) : Behavior<AttributeLimitsBehavior> {

    override fun mergeWith(higher: AttributeLimitsBehavior): AttributeLimitsBehavior = copy(
        attributeCountLimit = higher.attributeCountLimit ?: attributeCountLimit,
        attributeValueLengthLimit = higher.attributeValueLengthLimit ?: attributeValueLengthLimit,
    )
}
