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

/**
 * Reads the configuration supplied by every mechanism (DSL, YAML, envars), then resolves their
 * precedence via [BehaviorResolver].
 */
@OptIn(ExperimentalApi::class)
class OpenTelemetryConfigReader(
    envVarReader: EnvVarReader? = platformEnvVarReader(),
    private val declarativeConfigReader: DeclarativeBehaviorReader? = null,
    private val behaviorResolver: BehaviorResolver = BehaviorResolverImpl(),
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) {

    private val reportingEnvVarReader = envVarReader?.let { ReportingEnvVarReader(it, ::reportEnvironmentWarning) }

    /**
     * Resolves behavior against the DSL, YAML, and envars.
     *
     * The YAML file is read from [configFilePath], or `OTEL_CONFIG_FILE`. If it is not present it
     * has no effect.
     *
     * A `null` [declarativeConfigReader] means no reader was registered. Supplying a path through
     * [configFilePath] or `OTEL_CONFIG_FILE` then reports an error, and the file layer is unset.
     *
     * A `null` `envVarReader` means this platform does not read envars, so the envar layer is unset
     * and `OTEL_CONFIG_FILE` is not read.
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
        val dslBehavior = guard("read the DSL", fallback = null, toError = ::userCodeError, block = dsl)
        return try {
            behaviorResolver.resolve(
                envars = guard("read environment variables", fallback = null) {
                    reportingEnvVarReader?.let { OpenTelemetryEnvVars(it).toBehavior() }
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

    /**
     * Reads the `.instrumentation` node of the declarative config file, which is located as in
     * [read]. Returns `null` if no file is set or no reader is registered. A missing reader is
     * reported by [read], not here.
     *
     * This function never throws. A file that cannot be read is reported and treated as unset.
     */
    fun readInstrumentation(configFilePath: String? = null): ConfigProperties? {
        val reader = declarativeConfigReader ?: return null
        val path = resolveConfigFilePath(configFilePath) ?: return null
        return guard(
            "read the instrumentation config from '$path'",
            fallback = null,
            toError = ::configFileError,
        ) {
            reader.readInstrumentation(path)
        }
    }

    private fun resolveConfigFilePath(configFilePath: String?): String? =
        guard("read $CONFIG_FILE", fallback = null) {
            configFilePath ?: reportingEnvVarReader?.readString(CONFIG_FILE)
        }

    private fun readDeclarativeFile(configFilePath: String?): OpenTelemetryBehavior? {
        val path = resolveConfigFilePath(configFilePath) ?: return null
        val reader = declarativeConfigReader
        if (reader == null) {
            reportError(
                SdkError.ApiMisuse(
                    api = CONFIG_FILE,
                    message = "Ignoring declarative config file '$path': no reader is registered. Add the " +
                        "config-declarative artifact and call configFile(yamlConfigReader()).",
                    severity = SdkErrorSeverity.ERROR,
                )
            )
            return null
        }
        return guard(
            "read declarative config file '$path'",
            fallback = OpenTelemetryBehavior(),
            toError = ::configFileError,
        ) {
            reader.read(path)
        }
    }

    private inline fun <T> guard(
        action: String,
        fallback: T,
        toError: (Throwable, String) -> SdkError = ::sdkCodeError,
        block: () -> T,
    ): T = try {
        block()
    } catch (e: Throwable) {
        reportError(toError(e, "Failed to $action; falling back"))
        fallback
    }

    private fun report(e: Throwable, action: String) {
        reportError(sdkCodeError(e, "Failed to $action; falling back"))
    }

    private fun reportError(error: SdkError) {
        try {
            sdkErrorHandler.onError(error)
        } catch (_: Throwable) {
        }
    }

    private fun sdkCodeError(e: Throwable, message: String): SdkError =
        SdkError.SdkCodeError(cause = e, message = message, severity = SdkErrorSeverity.ERROR)

    private fun userCodeError(e: Throwable, message: String): SdkError =
        SdkError.UserCodeError(cause = e, message = message, severity = SdkErrorSeverity.ERROR)

    private fun configFileError(e: Throwable, message: String): SdkError =
        SdkError.ApiMisuse(api = CONFIG_FILE, message = "$message: $e", severity = SdkErrorSeverity.ERROR)

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
