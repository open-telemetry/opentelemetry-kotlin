package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.init.DeclarativeConfigReader

/**
 * Reads the declarative config file the SDK was pointed at.
 */
@ExperimentalApi
fun interface DeclarativeBehaviorReader : DeclarativeConfigReader {

    /**
     * Reads and parses the declarative config file at [path].
     *
     * Throws if the file cannot be read, or does not hold a valid configuration.
     */
    fun read(path: String): OpenTelemetryBehavior

    /**
     * Reads the `.instrumentation` node of the declarative config file at [path], or returns `null`
     * if this reader does not support it.
     *
     * Throws if the file cannot be read, or is not valid YAML.
     */
    fun readInstrumentation(path: String): ConfigProperties? = null
}

/**
 * Returns the reader behind a registered [DeclarativeConfigReader], or `null` and reports an
 * error if it did not come from an opentelemetry-kotlin artifact.
 */
@ExperimentalApi
fun DeclarativeConfigReader.toBehaviorReader(sdkErrorHandler: SdkErrorHandler): DeclarativeBehaviorReader? {
    if (this is DeclarativeBehaviorReader) {
        return this
    }
    try {
        sdkErrorHandler.onError(
            SdkError.ApiMisuse(
                api = "configFile",
                message = "Ignoring unsupported DeclarativeConfigReader ${this::class.simpleName}. Use " +
                    "yamlConfigReader() from config-declarative.",
                severity = SdkErrorSeverity.ERROR,
            )
        )
    } catch (_: Throwable) {
    }
    return null
}
