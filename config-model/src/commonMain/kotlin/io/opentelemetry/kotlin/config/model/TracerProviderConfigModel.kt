package io.opentelemetry.kotlin.config.model

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Configuration for tracing.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#tracer-provider
 */
@ExperimentalApi
data class TracerProviderConfigModel(

    /**
     * Limits on span data capture.
     */
    val spanLimits: SpanLimitsConfigModel? = null,
) : ConfigModel<TracerProviderConfigModel> {

    override fun mergeWith(higher: TracerProviderConfigModel): TracerProviderConfigModel = copy(
        spanLimits = mergeNode(spanLimits, higher.spanLimits),
    )
}
