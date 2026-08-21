package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.config.schema.model.SpanLimits

/**
 * Maps the `tracer_provider.limits` section of a declarative config file onto the behavior it
 * supplies. Anything the file omits, or sets to a value the spec disallows, is left unset.
 */
@ExperimentalApi
fun SpanLimits.toBehavior(): SpanLimitsBehavior = SpanLimitsBehavior(
    attributeCountLimit = limitOrUnset(attributeCountLimit),
    attributeValueLengthLimit = limitOrUnset(attributeValueLengthLimit),
    linkCountLimit = limitOrUnset(linkCountLimit),
    eventCountLimit = limitOrUnset(eventCountLimit),
    attributeCountPerEventLimit = limitOrUnset(eventAttributeCountLimit),
    attributeCountPerLinkLimit = limitOrUnset(linkAttributeCountLimit),
)
