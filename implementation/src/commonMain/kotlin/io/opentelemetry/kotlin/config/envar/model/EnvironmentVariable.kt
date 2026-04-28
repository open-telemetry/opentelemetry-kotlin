package io.opentelemetry.kotlin.config.envar.model

internal data class EnvironmentVariable<T>(val name: EnvVarName, val value: T?)
