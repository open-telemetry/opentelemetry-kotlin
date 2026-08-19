package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.config.schema.model.LogRecordLimits

/**
 * Maps the `logger_provider.limits` section of a declarative config file onto the behavior it
 * supplies. Anything the file omits, or sets to a value the spec disallows, is left unset.
 */
@ExperimentalApi
fun LogRecordLimits.toBehavior(): LogLimitsBehavior = LogLimitsBehavior(
    attributeCountLimit = limitOrUnset(attributeCountLimit),
    attributeValueLengthLimit = limitOrUnset(attributeValueLengthLimit),
)
