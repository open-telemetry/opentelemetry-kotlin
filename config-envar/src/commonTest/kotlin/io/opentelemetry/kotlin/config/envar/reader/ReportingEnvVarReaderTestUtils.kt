package io.opentelemetry.kotlin.config.envar.reader

internal fun reportingEnvVarReader(
    getEnvVar: (String) -> String?,
    onWarning: (EnvVarReadWarning) -> Unit = {},
): ReportingEnvVarReader = ReportingEnvVarReader(EnvVarReader(getEnvVar), onWarning)
