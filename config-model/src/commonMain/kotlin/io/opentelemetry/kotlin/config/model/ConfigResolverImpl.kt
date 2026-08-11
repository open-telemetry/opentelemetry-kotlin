package io.opentelemetry.kotlin.config.model

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
class ConfigResolverImpl : ConfigResolver {

    override fun resolve(
        envars: OpenTelemetryConfigModel?,
        declarativeFile: OpenTelemetryConfigModel?,
        dsl: OpenTelemetryConfigModel?,
    ): OpenTelemetryConfigModel {
        val layers = listOfNotNull(declarativeFile ?: envars, dsl)
        return mergeConfigModels(layers)
    }
}
