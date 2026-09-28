package io.opentelemetry.kotlin.config.envar.reader

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Unset
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value

/**
 * Reads environment variables, turning each raw value into the type the configuration expects.
 *
 * [getEnvVar] is supplied by the platform, so it is treated as hostile: a failure to read a
 * variable is treated the same way as an unset variable.
 */
@ExperimentalApi
class EnvVarReader(private val getEnvVar: (String) -> String?) {

    /**
     * Reads the value of [name] as an [Int].
     */
    fun readInt(name: String): EnvVarReadResult<Int> = readString(name).flatMap { value ->
        value.toIntOrNull()
            ?.let(::Value)
            ?: Invalid(EnvVarReadWarning(name, "Invalid integer value '$value'; ignoring"))
    }

    /**
     * Reads the value of [name] as a [Long].
     */
    fun readLong(name: String): EnvVarReadResult<Long> = readString(name).flatMap { value ->
        value.toLongOrNull()
            ?.let(::Value)
            ?: Invalid(EnvVarReadWarning(name, "Invalid long value '$value'; ignoring"))
    }

    /** Reads the value of [name] as a Boolean. */
    fun readBoolean(name: String): EnvVarReadResult<Boolean> = readString(name).flatMap { value ->
        when {
            value.equals("true", ignoreCase = true) -> Value(true)
            value.equals("false", ignoreCase = true) -> Value(false)
            else -> Value(
                value = false,
                warning = EnvVarReadWarning(name, "Invalid Boolean value '$value'; falling back to false"),
            )
        }
    }

    /**
     * Reads the value of [name]. An unset or blank value is [Unset].
     */
    fun readString(name: String): EnvVarReadResult<String> = try {
        getEnvVar(name)
            ?.takeUnless { it.isBlank() }
            ?.let(::Value)
            ?: Unset
    } catch (_: Throwable) {
        Unset
    }
}

/** Reads a non-negative integer value from [name]. */
@ExperimentalApi
internal fun EnvVarReader.readNonNegativeInt(name: String): EnvVarReadResult<Int> =
    readInt(name).flatMap { value ->
        if (value >= 0) {
            Value(value)
        } else {
            Invalid(EnvVarReadWarning(name, "Negative integer value '$value' is not allowed; ignoring"))
        }
    }

/** Reads a non-negative long value from [name]. */
@ExperimentalApi
internal fun EnvVarReader.readNonNegativeLong(name: String): EnvVarReadResult<Long> =
    readLong(name).flatMap { value ->
        if (value >= 0) {
            Value(value)
        } else {
            Invalid(EnvVarReadWarning(name, "Negative long value '$value' is not allowed; ignoring"))
        }
    }
