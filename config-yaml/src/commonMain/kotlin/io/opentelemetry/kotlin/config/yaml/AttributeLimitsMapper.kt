package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.config.schema.model.AttributeLimits

/**
 * Maps the top-level `attribute_limits` section of a declarative config file onto the behavior it
 * supplies. Anything the file omits, or sets to a value the spec disallows, is left unset.
 */
@ExperimentalApi
fun AttributeLimits.toBehavior(): AttributeLimitsBehavior = AttributeLimitsBehavior(
    attributeCountLimit = limitOrUnset(attributeCountLimit),
    attributeValueLengthLimit = limitOrUnset(attributeValueLengthLimit),
)
