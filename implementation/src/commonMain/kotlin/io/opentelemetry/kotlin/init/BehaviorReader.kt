package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.ConfigProperties
import io.opentelemetry.kotlin.config.DeclarativeBehaviorReader
import io.opentelemetry.kotlin.config.OpenTelemetryConfigReader
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.config.platformEnvVarReader
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Reads the behavior supplied by every configuration mechanism, then resolves their precedence.
 */
internal interface BehaviorReader {
    fun read(configFilePath: String?, dsl: () -> OpenTelemetryBehavior): OpenTelemetryBehavior

    /**
     * Reads the `.instrumentation` node of the declarative config file, or returns `null` if there
     * is none.
     */
    fun readInstrumentation(configFilePath: String?): ConfigProperties?
}

/**
 * Reads every mechanism the platform supports. The declarative config file is only read when a
 * [declarativeConfigReader] was registered.
 */
internal fun defaultBehaviorReader(
    envVarReader: EnvVarReader? = platformEnvVarReader(),
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    declarativeConfigReader: DeclarativeBehaviorReader? = null,
): BehaviorReader {
    val configReader = OpenTelemetryConfigReader(
        envVarReader = envVarReader,
        declarativeConfigReader = declarativeConfigReader,
        sdkErrorHandler = sdkErrorHandler,
    )
    return object : BehaviorReader {
        override fun read(configFilePath: String?, dsl: () -> OpenTelemetryBehavior): OpenTelemetryBehavior =
            configReader.read(dsl = dsl, configFilePath = configFilePath)

        override fun readInstrumentation(configFilePath: String?): ConfigProperties? =
            configReader.readInstrumentation(configFilePath)
    }
}
