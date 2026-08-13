package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Behavior of the SDK, as supplied by one configuration mechanism.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/
 */
@ExperimentalApi
data class OpenTelemetryBehavior(
    val resource: ResourceBehavior? = null,
    val tracerProvider: TracerProviderBehavior? = null,
) : Behavior<OpenTelemetryBehavior> {

    override fun mergeWith(higher: OpenTelemetryBehavior): OpenTelemetryBehavior = copy(
        resource = mergeNode(resource, higher.resource),
        tracerProvider = mergeNode(tracerProvider, higher.tracerProvider),
    )
}

/**
 * Combines [layers] into a single behavior, where each layer takes precedence over the ones before
 * it.
 */
@ExperimentalApi
fun mergeBehaviors(layers: List<OpenTelemetryBehavior>): OpenTelemetryBehavior =
    layers.fold(OpenTelemetryBehavior()) { merged, layer -> merged.mergeWith(layer) }
