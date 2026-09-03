package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.envar.logging.LogLimitsEnvVars
import io.opentelemetry.kotlin.config.envar.tracing.SpanLimitsEnvVars

/**
 * Maps every environment variable this SDK understands onto [OpenTelemetryBehavior].
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/
 */
@ExperimentalApi
class OpenTelemetryEnvVars(private val reader: EnvVarReader) {

    fun toBehavior(): OpenTelemetryBehavior = OpenTelemetryBehavior(
        attributeLimits = AttributeLimitsEnvVars(reader).toBehavior(),
        tracerProvider = TracerProviderBehavior(
            spanLimits = SpanLimitsEnvVars(reader).toBehavior(),
        ),
        loggerProvider = LoggerProviderBehavior(
            logLimits = LogLimitsEnvVars(reader).toBehavior(),
        ),
    )
}
