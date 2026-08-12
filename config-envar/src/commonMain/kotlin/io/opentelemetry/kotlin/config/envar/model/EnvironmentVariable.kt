package io.opentelemetry.kotlin.config.envar.model

data class EnvironmentVariable<T>(val name: EnvVarName, val value: T?)
