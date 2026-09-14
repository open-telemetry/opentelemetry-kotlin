package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.OpenTelemetryConfigReader
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import io.opentelemetry.kotlin.getEnvVarValue

/**
 * Reads the behavior supplied by every configuration mechanism, then resolves their precedence.
 */
internal fun interface BehaviorReader {
    fun read(configFilePath: String?, dsl: OpenTelemetryBehavior): OpenTelemetryBehavior
}

/**
 * Reads every mechanism the platform supports.
 */
internal fun defaultBehaviorReader(
    envVarReader: EnvVarReader = EnvVarReader(::getEnvVarValue),
    onSamplerWarning: (String) -> Unit = {},
): BehaviorReader {
    val configReader = OpenTelemetryConfigReader(
        envVarReader = envVarReader,
        onSamplerWarning = onSamplerWarning,
    )
    return BehaviorReader { configFilePath, dsl ->
        configReader.read(dsl = dsl, configFilePath = configFilePath)
    }
}
