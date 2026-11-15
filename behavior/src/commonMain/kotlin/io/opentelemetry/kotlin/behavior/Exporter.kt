package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
interface Exporter {
    /**
     * Console exporter.
     * */
    val console: ConsoleExporterBehavior?
    /**
     * HTTP exporter.
     * */
    val http: OtlpExporter?
}