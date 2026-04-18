package io.opentelemetry.kotlin.config.envar.model

import io.opentelemetry.kotlin.init.config.LogLimitConfig

internal data class EnvironmentConfiguration(
    val logLimitConfig: LogLimitConfig
)
