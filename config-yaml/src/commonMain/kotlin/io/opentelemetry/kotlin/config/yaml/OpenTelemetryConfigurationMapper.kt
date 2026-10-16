package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration
import io.opentelemetry.kotlin.config.yaml.logging.toBehavior
import io.opentelemetry.kotlin.config.yaml.tracing.toBehavior

/**
 * Maps a declarative config file onto the behavior it supplies, as one layer.
 */
@ExperimentalApi
fun OpenTelemetryConfiguration.toBehavior(): OpenTelemetryBehavior = OpenTelemetryBehavior(
    attributeLimits = attributeLimits?.toBehavior(),
    resource = resource?.toBehavior(),
    tracerProvider = tracerProvider?.toBehavior(),
    loggerProvider = loggerProvider?.let {
        LoggerProviderBehavior(
            logLimits = it.limits?.toBehavior(),
            processor = it.processors.toBehavior(),
        )
    },
)
