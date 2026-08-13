package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Behavior of the SDK, as supplied by one configuration mechanism.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/
 */
@ExperimentalApi
data class OpenTelemetryBehavior(
    val attributeLimits: AttributeLimitsBehavior? = null,
    val tracerProvider: TracerProviderBehavior? = null,
) : Behavior<OpenTelemetryBehavior> {

    override fun mergeWith(higher: OpenTelemetryBehavior): OpenTelemetryBehavior = copy(
        attributeLimits = mergeNode(attributeLimits, higher.attributeLimits),
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
