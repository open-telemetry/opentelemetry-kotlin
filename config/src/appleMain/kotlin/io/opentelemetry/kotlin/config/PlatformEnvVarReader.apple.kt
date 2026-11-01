package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader

@ExperimentalApi
actual fun platformEnvVarReader(): EnvVarReader? = null
