package io.opentelemetry.kotlin.config.envar

/**
 * No-Op Implementation as target should not support envar configuration
 */
internal actual fun getEnvVarValue(envVar: String): String? = null