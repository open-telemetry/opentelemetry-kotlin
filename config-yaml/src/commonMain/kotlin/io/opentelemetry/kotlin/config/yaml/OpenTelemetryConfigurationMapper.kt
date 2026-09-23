package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
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
            processor = it.processors.toProcessorBehavior(),
            sampler = it.sampler?.toBehavior(),
            idGenerator = it.idGenerator?.toBehavior(),
        )
    },
    loggerProvider = loggerProvider?.let {
        LoggerProviderBehavior(
            logLimits = it.limits?.toBehavior(),
            processor = it.processors.toBehavior(),
        )
    },
)

/**
 * Converts YAML processor configuration to SpanProcessorBehavior.
 * Since YAML specifies processor type (simple/batch), we preserve that information.
 */
@ExperimentalApi
private fun List<io.opentelemetry.kotlin.config.schema.model.SpanProcessor>.toProcessorBehavior(): SpanProcessorBehavior? {
    val firstProcessor = firstOrNull() ?: return null
    val exporter = toExporterBehavior() ?: return null

    return when {
        firstProcessor.simple != null -> SpanProcessorBehavior.Simple(exporter = exporter)
        firstProcessor.batch != null -> SpanProcessorBehavior.Batch(exporter = exporter)
        else -> null
    }
}
