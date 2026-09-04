package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration

/**
 * Maps a declarative config file onto the behavior it supplies, as one layer.
 */
@ExperimentalApi
fun OpenTelemetryConfiguration.toBehavior(): OpenTelemetryBehavior = OpenTelemetryBehavior(
    attributeLimits = attributeLimits?.toBehavior(),
    tracerProvider = tracerProvider?.let {
        TracerProviderBehavior(
            spanLimits = it.limits?.toBehavior(),
            sampler = it.sampler?.toBehavior()
        )
    },
    loggerProvider = loggerProvider?.let {
        LoggerProviderBehavior(logLimits = it.limits?.toBehavior())
    },
)
