package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Reads environment variables, turning each raw value into the type the configuration expects.
 *
 * A variable that is unset, or that holds a value which cannot be read as the requested type, is
 * reported as `null`, meaning the environment configured nothing for it. [getEnvVar] is supplied by
 * the platform, so it is treated as hostile: a failure to read a variable is reported the same way.
 */
@ExperimentalApi
class EnvVarReader(private val getEnvVar: (String) -> String?) {

    /**
     * Returns the value of [name] as an [Int], or `null` if it is unset or is not an [Int].
     */
    fun readInt(name: String): Int? = readString(name)?.toIntOrNull()

    /**
     * Returns the value of [name], or `null` if it is unset or could not be read.
     */
    fun readString(name: String): String? = try {
        getEnvVar(name)
    } catch (_: Throwable) {
        null
    }
}
