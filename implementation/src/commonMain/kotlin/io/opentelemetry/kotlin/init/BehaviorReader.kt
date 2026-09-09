package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.OpenTelemetryConfigReader

/**
 * Reads the behavior supplied by every configuration mechanism, then resolves their precedence.
 */
internal fun interface BehaviorReader {
    fun read(configFilePath: String?, dsl: OpenTelemetryBehavior): OpenTelemetryBehavior
}

/**
 * Reads every mechanism the platform supports.
 */
internal fun defaultBehaviorReader(): BehaviorReader {
    val configReader = OpenTelemetryConfigReader()
    return BehaviorReader { configFilePath, dsl ->
        configReader.read(dsl = dsl, configFilePath = configFilePath)
    }
}
