package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.config.envar.Exporter
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

@OptIn(ExperimentalApi::class)
class LogRecordExporterEnvVars(private val reader: ReportingEnvVarReader) {
    fun toBehavior(): LogRecordExporterBehavior? {
        return reader.readStringAndTransform(LOGS_EXPORTER) { name ->
            when (Exporter.fromValue(name.lowercase())) {
                Exporter.CONSOLE -> Value(
                    LogRecordExporterBehavior(console = ConsoleExporterBehavior())
                )
                Exporter.OTLP -> Value(
                    LogRecordExporterBehavior(
                        http = OtlpHttpLogRecordExporterEnvVars(reader).toBehavior()
                    )
                )
                null -> Invalid(EnvVarReadWarning(LOGS_EXPORTER, "Unknown value '$name'; ignoring"))
                else -> Value(null) // Known exporter, but not yet supported.
            }
        }
    }

    internal companion object {
        const val LOGS_EXPORTER = "OTEL_LOGS_EXPORTER"
    }
}
