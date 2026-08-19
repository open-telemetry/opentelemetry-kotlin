package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.init.AttributeLimitsConfigDsl

/**
 * Captures the global attribute limits configured programmatically, and maps them onto a behavior.
 */
@ExperimentalApi
class AttributeLimitsConfigDslImpl :
    AttributeLimitsConfigDsl,
    BehaviorSupplier<AttributeLimitsBehavior> {

    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null

    override fun toBehavior(): AttributeLimitsBehavior = AttributeLimitsBehavior(
        attributeCountLimit = limitOrUnset(attributeCountLimit),
        attributeValueLengthLimit = limitOrUnset(attributeValueLengthLimit),
    )
}
