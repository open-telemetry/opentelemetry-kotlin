package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps the span limit environment variables onto the behavior they supply. A variable that is unset,
 * or that holds a value the spec disallows, leaves its limit unset.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#span-limits
 */
@ExperimentalApi
class SpanLimitsEnvVars(
    private val reader: ReportingEnvVarReader,
) {

    fun toBehavior(): SpanLimitsBehavior = SpanLimitsBehavior(
        attributeCountLimit = reader.readNonNegativeInt(ATTRIBUTE_COUNT_LIMIT),
        attributeValueLengthLimit = reader.readNonNegativeInt(ATTRIBUTE_VALUE_LENGTH_LIMIT),
        linkCountLimit = reader.readNonNegativeInt(LINK_COUNT_LIMIT),
        eventCountLimit = reader.readNonNegativeInt(EVENT_COUNT_LIMIT),
        attributeCountPerEventLimit = reader.readNonNegativeInt(EVENT_ATTRIBUTE_COUNT_LIMIT),
        attributeCountPerLinkLimit = reader.readNonNegativeInt(LINK_ATTRIBUTE_COUNT_LIMIT),
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
