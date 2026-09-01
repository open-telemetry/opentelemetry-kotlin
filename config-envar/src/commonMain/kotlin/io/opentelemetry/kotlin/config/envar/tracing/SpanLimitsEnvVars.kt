package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.config.envar.EnvVarReader

/**
 * Maps the span limit environment variables onto the behavior they supply. A variable that is unset,
 * or that holds a value the spec disallows, leaves its limit unset.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#span-limits
 */
@ExperimentalApi
class SpanLimitsEnvVars(private val reader: EnvVarReader) {

    fun toBehavior(): SpanLimitsBehavior = SpanLimitsBehavior(
        attributeCountLimit = limitOrUnset(reader.readInt(ATTRIBUTE_COUNT_LIMIT)),
        attributeValueLengthLimit = limitOrUnset(reader.readInt(ATTRIBUTE_VALUE_LENGTH_LIMIT)),
        linkCountLimit = limitOrUnset(reader.readInt(LINK_COUNT_LIMIT)),
        eventCountLimit = limitOrUnset(reader.readInt(EVENT_COUNT_LIMIT)),
        attributeCountPerEventLimit = limitOrUnset(reader.readInt(EVENT_ATTRIBUTE_COUNT_LIMIT)),
        attributeCountPerLinkLimit = limitOrUnset(reader.readInt(LINK_ATTRIBUTE_COUNT_LIMIT)),
    )

    private companion object {
        const val ATTRIBUTE_COUNT_LIMIT = "OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT"
        const val ATTRIBUTE_VALUE_LENGTH_LIMIT = "OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT"
        const val LINK_COUNT_LIMIT = "OTEL_SPAN_LINK_COUNT_LIMIT"
        const val EVENT_COUNT_LIMIT = "OTEL_SPAN_EVENT_COUNT_LIMIT"
        const val EVENT_ATTRIBUTE_COUNT_LIMIT = "OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT"
        const val LINK_ATTRIBUTE_COUNT_LIMIT = "OTEL_LINK_ATTRIBUTE_COUNT_LIMIT"
    }
}
