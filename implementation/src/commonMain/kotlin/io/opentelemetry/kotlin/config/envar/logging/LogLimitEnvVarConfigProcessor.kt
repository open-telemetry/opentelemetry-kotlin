package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.config.envar.processor.EnvVarConfigProcessor
import io.opentelemetry.kotlin.init.config.LogLimitConfig

/**
 * Configures LogRecord Limits based on env vars
 */
internal abstract class LogLimitEnvVarConfigProcessor : EnvVarConfigProcessor<LogLimitConfig, Int>()
