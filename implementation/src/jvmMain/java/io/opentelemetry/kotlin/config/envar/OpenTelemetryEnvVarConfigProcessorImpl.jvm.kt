package io.opentelemetry.kotlin.config.envar

internal actual fun getEnvVarValue(envVar: String): String? = System.getenv(envVar)
