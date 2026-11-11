package io.opentelemetry.kotlin.config.envar.reader

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * The result of reading and parsing an environment variable.
 */
@OptIn(ExperimentalApi::class)
sealed interface EnvVarReadResult<out T> {
    /** The environment variable contains a valid [value]. */
    data class Value<T>(
        val value: T,
        val warning: EnvVarReadWarning? = null,
    ) : EnvVarReadResult<T>

    /** The environment variable is unset or blank. */
    data object Unset : EnvVarReadResult<Nothing>

    /** The environment variable contains a value that cannot be used. */
    data class Invalid(val warning: EnvVarReadWarning) : EnvVarReadResult<Nothing>
}

/** A warning produced while reading an environment variable. */
@OptIn(ExperimentalApi::class)
data class EnvVarReadWarning(
    val name: String,
    val message: String,
)

@OptIn(ExperimentalApi::class)
internal inline fun <T, R> EnvVarReadResult<T>.flatMap(
    transform: (T) -> EnvVarReadResult<R>,
): EnvVarReadResult<R> = when (this) {
    is EnvVarReadResult.Value -> transform(value)
    EnvVarReadResult.Unset -> EnvVarReadResult.Unset
    is EnvVarReadResult.Invalid -> this
}
