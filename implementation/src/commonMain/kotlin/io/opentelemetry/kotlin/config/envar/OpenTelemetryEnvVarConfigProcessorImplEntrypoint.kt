package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessorImpl
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
        envVars = EnvVarConstants.LogLimits.envVars
    )
    return OpenTelemetryEnvVarConfigProcessorImpl(
        loggingConfig = cfg.generateLoggingConfig(),
        logLimitProcessor = logLimitProcessor
    )
}
