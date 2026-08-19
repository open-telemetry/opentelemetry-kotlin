package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.limitOrUnset
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName

/**
 * Maps the global attribute limit environment variables onto the behavior they supply. A variable
 * that is unset, or that holds a value the spec disallows, leaves its limit unset.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#attribute-limits
 */
@ExperimentalApi
class AttributeLimitsEnvVars(private val reader: EnvVarReader) {

    fun toBehavior(): AttributeLimitsBehavior = AttributeLimitsBehavior(
        attributeCountLimit = limitOrUnset(reader.readInt(ATTRIBUTE_COUNT_LIMIT)),
        attributeValueLengthLimit = limitOrUnset(reader.readInt(ATTRIBUTE_VALUE_LENGTH_LIMIT)),
    )

    private companion object {
        val ATTRIBUTE_COUNT_LIMIT = envVarName("OTEL_ATTRIBUTE_COUNT_LIMIT")
        val ATTRIBUTE_VALUE_LENGTH_LIMIT = envVarName("OTEL_ATTRIBUTE_VALUE_LENGTH_LIMIT")
    }
}
