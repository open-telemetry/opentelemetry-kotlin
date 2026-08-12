package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName

object EnvVarConstants {
    sealed interface EnvVarLimits {
        val envVars: List<EnvVarName>
    }

    object LogLimits : EnvVarLimits {
        private val ATTR_COUNT_LIMIT = envVarName("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT")
        private val ATTR_VALUE_LENGTH_LIMIT =
            envVarName("OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT")

        override val envVars = listOf(ATTR_COUNT_LIMIT, ATTR_VALUE_LENGTH_LIMIT)
    }

    object SpanLimits : EnvVarLimits {
        private val ATTR_COUNT_LIMIT = envVarName("OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT")
        private val ATTR_VALUE_LENGTH_LIMIT =
            envVarName("OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT")
        private val EVENT_COUNT_LIMIT = envVarName("OTEL_SPAN_EVENT_COUNT_LIMIT")
        private val LINK_COUNT_LIMIT = envVarName("OTEL_SPAN_LINK_COUNT_LIMIT")
        private val EVENT_ATTR_COUNT_LIMIT = envVarName("OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT")
        private val LINK_ATTR_COUNT_LIMIT = envVarName("OTEL_LINK_ATTRIBUTE_COUNT_LIMIT")

        override val envVars = listOf(
            ATTR_COUNT_LIMIT,
            ATTR_VALUE_LENGTH_LIMIT,
            EVENT_COUNT_LIMIT,
            LINK_COUNT_LIMIT,
            EVENT_ATTR_COUNT_LIMIT,
            LINK_ATTR_COUNT_LIMIT,
        )
    }
}
