package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName

internal object EnvVarConstants {
    internal sealed interface EnvVarLimits {
        val envVars: List<EnvVarName>
    }

    internal object LogLimits: EnvVarLimits {
        private val ATTR_COUNT_LIMIT = envVarName("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT")
        private val ATTR_VALUE_LENGTH_LIMIT = envVarName("OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT")

        override val envVars = listOf(
            ATTR_COUNT_LIMIT, ATTR_VALUE_LENGTH_LIMIT
        )
    }
}