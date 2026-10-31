package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader

/**
 * The reader for envars on the platform the SDK is running on, or `null` where the SDK does not
 * read them. Envars are read on the JVM, Linux, and Node.js, but not on Android, Apple, or browser
 * platforms.
 */
@ExperimentalApi
expect fun platformEnvVarReader(): EnvVarReader?
