package io.opentelemetry.kotlin.config.envar

/**
 * No-Op Implementation as target should not support envar configuration
 */
actual fun getEnvVarValue(envVar: String): String? = null
