package io.opentelemetry.kotlin.config.envar.processor

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentVariable


@ExperimentalApi
internal abstract class EnvVarConfigProcessor<R, T> {
    /**
     * list of env vars
     */
    protected abstract val envVars: List<EnvVarName>

    /**
     * Parse raw value from system
     */
    protected abstract fun parse(value: String?): T?


    /**
     * processes total list of env vars to be specific
     */
    protected abstract fun process(
        entries: Map<EnvVarName, EnvironmentVariable<T>>,
        defaultValue: R? = null
    ): R

    fun resolve(defaultValue: R? = null, getRawValue: (String) -> String?): R =
        envVars
            .associateWith { EnvironmentVariable(it, parse(getRawValue(it.value))) }
            .let { process(it, defaultValue)}
}