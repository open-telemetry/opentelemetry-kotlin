package io.opentelemetry.kotlin.config.envar

expect fun getEnvVarValue(envVar: String): String?
