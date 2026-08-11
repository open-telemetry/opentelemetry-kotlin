package io.opentelemetry.kotlin.config.envar

actual fun getEnvVarValue(envVar: String): String? = System.getenv(envVar)
