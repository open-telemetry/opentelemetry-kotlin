package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.model.EnvironmentConfiguration

/**
 * Processes environment configuration
 */
@ExperimentalApi
internal sealed interface OpenTelemetryEnvVarConfigProcessor {
    fun process(): EnvironmentConfiguration
}