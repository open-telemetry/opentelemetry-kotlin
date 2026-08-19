package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Resolves the configuration supplied by each mechanism into the single behavior the SDK is
 * initialized with.
 */
@ExperimentalApi
interface BehaviorResolver {

    /**
     * Resolves the supplied layers, where `null` means the mechanism supplied no configuration.
     *
     * Precedence is `SDK defaults < (envars or declarative config file) < DSL`. [envars] are
     * ignored entirely when [declarativeFile] is present.
     *
     * Anything left unset in the result was configured by no mechanism, and is left to the default
     * the SDK being initialized already applies.
     */
    fun resolve(
        envars: OpenTelemetryBehavior?,
        declarativeFile: OpenTelemetryBehavior?,
        dsl: OpenTelemetryBehavior?,
    ): OpenTelemetryBehavior
}
