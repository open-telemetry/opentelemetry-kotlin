package io.opentelemetry.kotlin.behavior.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Exporters for the logger provider. These are independent of the kind of processor (simple or
 * batch) that feeds them.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecordexporter
 */
@ExperimentalApi
data class LogRecordExporterBehavior(

    /**
     * Console log exporter.
     * */
    val console: ConsoleExporterBehavior? = null,

    /**
     * HTTP log exporter.
     * */
    val http: OtlpHttpLogsExporterBehavior? = null,

    ) : Behavior<LogRecordExporterBehavior> {

    override fun mergeWith(higher: LogRecordExporterBehavior): LogRecordExporterBehavior = copy(
        console = mergeNode(console, higher.console),
        http = mergeNode(http, higher.http),
    )
}
