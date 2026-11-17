package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
data class ExporterDelegate<T : OtlpExporter>(
    override val console: ConsoleExporterBehavior? = null,
    override val http: T? = null
) : Exporter
