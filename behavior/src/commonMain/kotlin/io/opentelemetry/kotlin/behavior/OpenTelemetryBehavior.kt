package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Behavior of the SDK, as supplied by one configuration mechanism.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/
 */
@ExperimentalApi
data class OpenTelemetryBehavior(
    val tracerProvider: TracerProviderBehavior? = null,
    val loggerProvider: LoggerProviderBehavior? = null,
) : Behavior<OpenTelemetryBehavior> {

    override fun mergeWith(higher: OpenTelemetryBehavior): OpenTelemetryBehavior = copy(
        tracerProvider = mergeNode(tracerProvider, higher.tracerProvider),
        loggerProvider = mergeNode(loggerProvider, higher.loggerProvider),
    )
}

/**
 * Combines [layers] into a single behavior, where each layer takes precedence over the ones before
 * it.
 */
@ExperimentalApi
fun mergeBehaviors(layers: List<OpenTelemetryBehavior>): OpenTelemetryBehavior =
    layers.fold(OpenTelemetryBehavior()) { merged, layer -> merged.mergeWith(layer) }
