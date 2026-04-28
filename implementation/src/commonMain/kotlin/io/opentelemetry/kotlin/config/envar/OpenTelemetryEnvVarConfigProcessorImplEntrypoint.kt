package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessorImpl
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl

@ExperimentalApi
internal fun createOpenTelemetryEnvVarConfigProcessor(
    /**
     * Defines the [Clock] implementation used by OpenTelemetry.
     */
    clock: Clock,

    /**
     * Defines configuration for OpenTelemetry.
     */
    config: OpenTelemetryConfigDsl.() -> Unit
): OpenTelemetryEnvVarConfigProcessor {
    return createOpenTelemetryEnvVarConfigProcessorImpl(clock, config)
}

@ExperimentalApi
internal fun createOpenTelemetryEnvVarConfigProcessorImpl(
    clock: Clock,
    config: OpenTelemetryConfigDsl.() -> Unit
): OpenTelemetryEnvVarConfigProcessor {
    val cfg = OpenTelemetryConfigImpl(clock).apply(config)
    val logLimitProcessor = LogLimitEnvVarConfigProcessorImpl(
        envVars = logLimitEnvars()
    )
    return OpenTelemetryEnvVarConfigProcessorImpl(
        loggingConfig = cfg.generateLoggingConfig(),
        logLimitProcessor = logLimitProcessor
    )
}

internal fun logLimitEnvars() = listOf(
    envVarName("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT"),
    envVarName("OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT")
)
