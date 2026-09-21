package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BehaviorResolver
import io.opentelemetry.kotlin.behavior.BehaviorResolverImpl
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.getEnvVarValue

/**
 * Reads the configuration supplied by every mechanism (DSL, YAML, envars), then resolves their
 * precedence via [BehaviorResolver].
 */
@ExperimentalApi
class OpenTelemetryConfigReader(
    private val envVarReader: EnvVarReader = EnvVarReader(::getEnvVarValue),
    private val declarativeConfigReader: DeclarativeConfigReader? = platformDeclarativeConfigReader(),
    private val behaviorResolver: BehaviorResolver = BehaviorResolverImpl(),
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) {

    /**
     * Resolves behavior against the DSL, YAML, and envars.
     *
     * The YAML file is read from [configFilePath], or `OTEL_CONFIG_FILE`. If it is not present it
     * has no effect, but if it is present but invalid then this function will throw.
     *
     * A `null` [declarativeConfigReader] means this platform does not read declarative config
     * files at all, so neither [configFilePath] nor `OTEL_CONFIG_FILE` is acted on.
     */
    fun read(
        dsl: OpenTelemetryBehavior? = null,
        configFilePath: String? = null,
    ): OpenTelemetryBehavior = behaviorResolver.resolve(
        envars = OpenTelemetryEnvVars(envVarReader, ::reportSamplerWarning).toBehavior(),
        declarativeFile = declarativeConfigReader?.let { reader ->
            val path = configFilePath ?: envVarReader.readString(CONFIG_FILE)
            path?.let(reader::read)
        },
        dsl = dsl,
    )

    private fun reportSamplerWarning(message: String) {
        sdkErrorHandler.onError(
            SdkError.ApiMisuse(
                api = "OTEL_TRACES_SAMPLER",
                message = message,
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }

    private companion object {
        const val CONFIG_FILE = "OTEL_CONFIG_FILE"
    }
}
