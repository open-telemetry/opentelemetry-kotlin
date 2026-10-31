package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.getEnvVarValue

@ExperimentalApi
actual fun platformEnvVarReader(): EnvVarReader? = EnvVarReader(::getEnvVarValue)
