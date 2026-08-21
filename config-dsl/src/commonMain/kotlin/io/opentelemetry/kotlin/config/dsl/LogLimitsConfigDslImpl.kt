package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.init.LogLimitsConfigDsl

/**
 * Captures the log record limits configured programmatically, and maps them onto a behavior.
 */
@ExperimentalApi
class LogLimitsConfigDslImpl : LogLimitsConfigDsl, BehaviorSupplier<LogLimitsBehavior> {

    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null

    override fun toBehavior(): LogLimitsBehavior = LogLimitsBehavior(
        attributeCountLimit = limitOrUnset(attributeCountLimit),
        attributeValueLengthLimit = limitOrUnset(attributeValueLengthLimit),
    )
}
