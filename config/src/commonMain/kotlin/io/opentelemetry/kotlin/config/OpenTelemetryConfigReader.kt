package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BehaviorResolver
import io.opentelemetry.kotlin.behavior.BehaviorResolverImpl
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader
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
    envVarReader: EnvVarReader = EnvVarReader(::getEnvVarValue),
    private val declarativeConfigReader: DeclarativeConfigReader? = platformDeclarativeConfigReader(),
    private val behaviorResolver: BehaviorResolver = BehaviorResolverImpl(),
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) {

    private val reportingEnvVarReader = ReportingEnvVarReader(envVarReader, ::reportEnvironmentWarning)

    /**
     * Resolves behavior against the DSL, YAML, and envars.
     *
     * The YAML file is read from [configFilePath], or `OTEL_CONFIG_FILE`. If it is not present it
     * has no effect.
     *
     * A `null` [declarativeConfigReader] means this platform does not read declarative config
     * files at all, so neither [configFilePath] nor `OTEL_CONFIG_FILE` is acted on.
     *
     * This function never throws. Exceptions in the DSL, YAML, or envar layers treats the layer as
     * unset.
     */
    fun read(
        dsl: OpenTelemetryBehavior? = null,
        configFilePath: String? = null,
    ): OpenTelemetryBehavior = read(dsl = { dsl }, configFilePath = configFilePath)

    fun read(
        dsl: () -> OpenTelemetryBehavior?,
        configFilePath: String? = null,
    ): OpenTelemetryBehavior {
        val dslBehavior = guard("read the DSL", fallback = null, block = dsl)
        return try {
            behaviorResolver.resolve(
                envars = guard("read environment variables", fallback = null) {
                    OpenTelemetryEnvVars(reportingEnvVarReader).toBehavior()
                },
                declarativeFile = readDeclarativeFile(configFilePath),
                dsl = dslBehavior,
            )
        } catch (e: Throwable) {
            report(e, "resolve configuration")
            try {
                behaviorResolver.resolve(envars = null, declarativeFile = null, dsl = dslBehavior)
            } catch (_: Throwable) {
                dslBehavior ?: OpenTelemetryBehavior()
            }
        }
    }

    private fun readDeclarativeFile(configFilePath: String?): OpenTelemetryBehavior? {
        val reader = declarativeConfigReader ?: return null
        val path = guard("read $CONFIG_FILE", fallback = null) {
            configFilePath ?: reportingEnvVarReader.readString(CONFIG_FILE)
        } ?: return null
        return guard("read declarative config file '$path'", fallback = OpenTelemetryBehavior()) {
            reader.read(path)
        }
    }

    private inline fun <T> guard(action: String, fallback: T, block: () -> T): T = try {
        block()
    } catch (e: Throwable) {
        report(e, action)
        fallback
    }

    private fun report(e: Throwable, action: String) {
        try {
            sdkErrorHandler.onError(
                SdkError.SdkCodeError(
                    cause = e,
                    message = "Failed to $action; falling back",
                    severity = SdkErrorSeverity.ERROR,
                )
            )
        } catch (_: Throwable) {
        }
    }

    private fun reportEnvironmentWarning(warning: EnvVarReadWarning) {
        sdkErrorHandler.onError(
            SdkError.ApiMisuse(
                api = warning.name,
                message = warning.message,
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }

    private companion object {
        const val CONFIG_FILE = "OTEL_CONFIG_FILE"
    }
}
