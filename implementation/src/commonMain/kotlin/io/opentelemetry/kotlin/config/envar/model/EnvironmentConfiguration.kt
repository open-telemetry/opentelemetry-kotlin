package io.opentelemetry.kotlin.config.envar.model

import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.init.config.SpanLimitConfig

internal data class EnvironmentConfiguration(
    val logLimitConfig: LogLimitConfig,
    val spanLimitConfig: SpanLimitConfig,
)
