package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps `OTEL_TRACES_SAMPLER` onto behavior.
 * Unrecognized sampler names are ignored and reported as warnings.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#general-sdk-configuration
 */
@ExperimentalApi
class SamplerEnvVars(private val reader: ReportingEnvVarReader) {

    fun toBehavior(): SamplerBehavior? = reader.readStringAndTransform(SAMPLER) { name ->
        when (name.lowercase()) {
            ALWAYS_ON -> Value(SamplerBehavior.AlwaysOn)
            ALWAYS_OFF -> Value(SamplerBehavior.AlwaysOff)
            PARENT_BASED_ALWAYS_ON -> Value(SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOn))
            PARENT_BASED_ALWAYS_OFF -> Value(SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff))
            else -> Invalid(EnvVarReadWarning(SAMPLER, "Unknown value '$name'; ignoring"))
        }
    }

    private companion object {
        const val SAMPLER = "OTEL_TRACES_SAMPLER"
        const val ALWAYS_ON = "always_on"
        const val ALWAYS_OFF = "always_off"
        const val PARENT_BASED_ALWAYS_ON = "parentbased_always_on"
        const val PARENT_BASED_ALWAYS_OFF = "parentbased_always_off"
    }
}
