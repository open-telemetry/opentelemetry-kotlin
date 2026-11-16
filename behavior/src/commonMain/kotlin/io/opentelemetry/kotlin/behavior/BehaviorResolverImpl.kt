package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class BehaviorResolverImpl : BehaviorResolver {

    override fun resolve(
        envars: OpenTelemetryBehavior?,
        declarativeFile: OpenTelemetryBehavior?,
        dsl: OpenTelemetryBehavior?,
    ): OpenTelemetryBehavior {
        val layers = listOfNotNull(declarativeFile ?: envars, dsl)
        return mergeBehaviors(layers).applyGlobalAttributeLimits()
    }
}
