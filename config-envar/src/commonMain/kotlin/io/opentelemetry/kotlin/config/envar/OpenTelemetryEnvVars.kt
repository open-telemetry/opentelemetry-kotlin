package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.envar.logging.LogLimitsEnvVars

/**
 * Gathers the environment variables the SDK reads into a single [OpenTelemetryBehavior].
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/
 */
@ExperimentalApi
class OpenTelemetryEnvVars(private val reader: EnvVarReader) {

    fun toBehavior(): OpenTelemetryBehavior = OpenTelemetryBehavior(
        attributeLimits = AttributeLimitsEnvVars(reader).toBehavior(),
        loggerProvider = LoggerProviderBehavior(logLimits = LogLimitsEnvVars(reader).toBehavior()),
    )
}
