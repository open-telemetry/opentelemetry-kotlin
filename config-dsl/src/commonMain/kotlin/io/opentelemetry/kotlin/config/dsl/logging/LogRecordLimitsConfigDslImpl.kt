package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BehaviorSupplier
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.behavior.logging.LogRecordLimitsBehavior
import io.opentelemetry.kotlin.init.LogRecordLimitsConfigDsl

/**
 * Captures the log record limits configured programmatically, and maps them onto a behavior.
 */
@OptIn(ExperimentalApi::class)
class LogRecordLimitsConfigDslImpl : LogRecordLimitsConfigDsl, BehaviorSupplier<LogRecordLimitsBehavior> {

    override var attributeCountLimit: Int? = null
    override var attributeValueLengthLimit: Int? = null

    override fun toBehavior(): LogRecordLimitsBehavior = LogRecordLimitsBehavior(
        attributeCountLimit = limitOrUnset(attributeCountLimit),
        attributeValueLengthLimit = limitOrUnset(attributeValueLengthLimit),
    )
}
