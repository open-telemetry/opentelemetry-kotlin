package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.OpenTelemetryConfigReader
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.config.platformEnvVarReader
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Reads the behavior supplied by every configuration mechanism, then resolves their precedence.
 */
internal fun interface BehaviorReader {
    fun read(configFilePath: String?, dsl: () -> OpenTelemetryBehavior): OpenTelemetryBehavior
}

/**
 * Reads every mechanism the platform supports.
 */
internal fun defaultBehaviorReader(
    envVarReader: EnvVarReader? = platformEnvVarReader(),
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
): BehaviorReader {
    val configReader = OpenTelemetryConfigReader(
        envVarReader = envVarReader,
        sdkErrorHandler = sdkErrorHandler,
    )
    return BehaviorReader { configFilePath, dsl ->
        configReader.read(dsl = dsl, configFilePath = configFilePath)
    }
}
