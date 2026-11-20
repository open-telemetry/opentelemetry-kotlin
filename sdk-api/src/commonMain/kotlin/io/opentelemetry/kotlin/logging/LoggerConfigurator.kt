package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Dynamically controls how a [Logger] should behave by constructing a [LoggerConfig].
 * Implementations must return quickly as this code may be invoked frequently.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#loggerconfigurator
 */
@ExperimentalApi
@ThreadSafe
public fun interface LoggerConfigurator {

    /**
     * Returns the [LoggerConfig] for the given [scope].
     */
    public fun loggerConfig(scope: InstrumentationScopeInfo): LoggerConfig
}
