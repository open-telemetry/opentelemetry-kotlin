package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.model.EnvironmentConfiguration

/**
 * Processes environment configuration
 */
internal sealed interface OpenTelemetryEnvVarConfigProcessor {
    fun process(): EnvironmentConfiguration
}