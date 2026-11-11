package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps `OTEL_TRACES_SAMPLER` and `OTEL_TRACES_SAMPLER_ARG` onto behavior.
 * Unrecognized sampler names are ignored and reported as warnings. An invalid ratio in
 * `OTEL_TRACES_SAMPLER_ARG` is reported and replaced by the default of 1.0.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#general-sdk-configuration
 */
@OptIn(ExperimentalApi::class)
class SamplerEnvVars(private val reader: ReportingEnvVarReader) {

    fun toBehavior(): SamplerBehavior? = reader.readStringAndTransform(SAMPLER) { name ->
        when (name.lowercase()) {
            ALWAYS_ON -> Value(SamplerBehavior.AlwaysOn)
            ALWAYS_OFF -> Value(SamplerBehavior.AlwaysOff)
            PARENT_BASED_ALWAYS_ON -> Value(SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOn))
            PARENT_BASED_ALWAYS_OFF -> Value(SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff))
            TRACE_ID_RATIO -> Value(SamplerBehavior.Probability(readRatio()))
            PARENT_BASED_TRACE_ID_RATIO ->
                Value(SamplerBehavior.ParentBased(root = SamplerBehavior.Probability(readRatio())))
            else -> Invalid(EnvVarReadWarning(SAMPLER, "Unknown value '$name'; ignoring"))
        }
    }

    private fun readRatio(): Double = reader.readStringAndTransform(SAMPLER_ARG) { arg ->
        val ratio = arg.trim().toDoubleOrNull()
        if (ratio != null && ratio in 0.0..1.0) {
            Value(ratio)
        } else {
            Value(DEFAULT_RATIO, EnvVarReadWarning(SAMPLER_ARG, "Invalid ratio '$arg'; using $DEFAULT_RATIO"))
        }
    } ?: DEFAULT_RATIO

    internal companion object {
        const val SAMPLER = "OTEL_TRACES_SAMPLER"
        const val ALWAYS_ON = "always_on"
        const val ALWAYS_OFF = "always_off"
        const val PARENT_BASED_ALWAYS_ON = "parentbased_always_on"
        const val PARENT_BASED_ALWAYS_OFF = "parentbased_always_off"
        const val TRACE_ID_RATIO = "traceidratio"
        const val PARENT_BASED_TRACE_ID_RATIO = "parentbased_traceidratio"
        const val SAMPLER_ARG = "OTEL_TRACES_SAMPLER_ARG"
        const val DEFAULT_RATIO = 1.0
    }
}
