package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.envar.EnvVarReader

/**
 * Maps `OTEL_TRACES_SAMPLER` onto behavior.
 * Unrecognized sampler names are ignored (and reported via [onWarning]).
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#general-sdk-configuration
 */
@ExperimentalApi
class SamplerEnvVars(
    private val reader: EnvVarReader,
    private val onWarning: (String) -> Unit = {},
) {
    fun toBehavior(): SamplerBehavior? {
        val name = reader.readString(SAMPLER)?.takeIf { it.isNotEmpty() } ?: return null
        return when (name.lowercase()) {
            ALWAYS_ON -> SamplerBehavior.AlwaysOn
            ALWAYS_OFF -> SamplerBehavior.AlwaysOff
            PARENT_BASED_ALWAYS_ON -> SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOn)
            PARENT_BASED_ALWAYS_OFF -> SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff)
            else -> null.also { onWarning("Unknown OTEL_TRACES_SAMPLER value '$name'; ignoring") }
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
