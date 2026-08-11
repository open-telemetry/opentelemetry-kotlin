package io.opentelemetry.kotlin.config.model

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Configuration for the SDK, as supplied by one configuration mechanism.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/
 */
@ExperimentalApi
data class OpenTelemetryConfigModel(
    val tracerProvider: TracerProviderConfigModel? = null,
) : ConfigModel<OpenTelemetryConfigModel> {

    override fun mergeWith(higher: OpenTelemetryConfigModel): OpenTelemetryConfigModel = copy(
        tracerProvider = mergeNode(tracerProvider, higher.tracerProvider),
    )
}

/**
 * Combines [layers] into a single model, where each layer takes precedence over the ones before it.
 */
@ExperimentalApi
fun mergeConfigModels(layers: List<OpenTelemetryConfigModel>): OpenTelemetryConfigModel =
    layers.fold(OpenTelemetryConfigModel()) { merged, layer -> merged.mergeWith(layer) }
