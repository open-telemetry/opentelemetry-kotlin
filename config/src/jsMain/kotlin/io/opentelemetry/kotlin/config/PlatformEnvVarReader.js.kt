package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.getEnvVarValue

/**
 * Envars are read from `process.env`, which is only available on Node.js. A browser, where
 * `process` is undefined, gets `null`.
 */
@ExperimentalApi
actual fun platformEnvVarReader(): EnvVarReader? = if (hasProcess()) {
    EnvVarReader(::getEnvVarValue)
} else {
    null
}

@Suppress("UnsafeCastFromDynamic")
private fun hasProcess(): Boolean = js("typeof process !== 'undefined' && !!process")
