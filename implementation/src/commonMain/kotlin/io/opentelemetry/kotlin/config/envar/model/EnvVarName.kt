package io.opentelemetry.kotlin.config.envar.model

internal value class EnvVarName(val value: String) {
    init {
        require(value.startsWith("OTEL"))
    }

    companion object {
        fun envVarName(value: String) = EnvVarName(value)
    }
}