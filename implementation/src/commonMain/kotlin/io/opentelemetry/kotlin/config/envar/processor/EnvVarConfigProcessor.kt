package io.opentelemetry.kotlin.config.envar.processor

import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentVariable

/**
 * Configuration processor, defining how specific parts of the library should be configured
 * via environment variables.

 * @param R is the resulting config type
 * @param T is the environment variable type
 */
internal abstract class EnvVarConfigProcessor<R, T> {
    /**
     * List of specific env vars
     */
    protected abstract val envVars: List<EnvVarName>

    /**
     * Parse the raw value from the system environment.
     * @param T is the environment variable type
     * @return an instance of type T or null if it's not found
     */
    protected abstract fun parse(rawValue: String?): T?

    /**
     * Processes the list of env vars to return a configuration object.
     *
     * @param entries all specific environment variables related to a library configuration
     * @param defaultValue the default value returned
     * @param R is the resulting config type
     * @param T is the environment variable type
     * @return a configuration object built with envars, or with the given default values if the environment variable does not exist or is not set.
     */
    protected abstract fun process(
        entries: Map<EnvVarName, EnvironmentVariable<T>>,
        defaultValue: R
    ): R

    /**
     * Process specific environment variables and configure a library configuration object
     * @param defaultValue a default value, returned if a given value cannot be retrieved during processing
     * @param getRawValue is the function which returns your system related (raw) envar value
     * @return a configuration object related to specific a library section
     */
    fun configure(defaultValue: R, getRawValue: ((String) -> String?)? = null): R =
        envVars
            .associateWith { EnvironmentVariable(it, parse(getRawValue?.invoke(it.value))) }
            .let { process(it, defaultValue) }
}
