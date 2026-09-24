package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.OpenTelemetryConfigReader
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Reads the behavior supplied by every configuration mechanism, then resolves their precedence.
 */
@ExperimentalApi
internal fun interface CompatBehaviorReader {
    fun read(configFilePath: String?, dsl: OpenTelemetryBehavior): OpenTelemetryBehavior
}

/**
 * Reads every mechanism the platform supports. Declarative configuration files are read on the JVM
 * only.
 */
@ExperimentalApi
internal fun defaultCompatBehaviorReader(
    envVarReader: EnvVarReader = EnvVarReader { System.getenv(it) },
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
): CompatBehaviorReader {
    val configReader = OpenTelemetryConfigReader(
        envVarReader = envVarReader,
        sdkErrorHandler = sdkErrorHandler,
    )
    return CompatBehaviorReader { configFilePath, dsl ->
        configReader.read(dsl = dsl, configFilePath = configFilePath)
    }
}
