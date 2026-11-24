package io.opentelemetry.kotlin.behavior.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.Exporter
import io.opentelemetry.kotlin.behavior.ExporterDelegate
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Exporters for the logger provider. These are independent of the kind of processor (simple or
 * batch) that feeds them.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecordexporter
 */
@OptIn(ExperimentalApi::class)
data class LogRecordExporterBehavior(
    val delegate: ExporterDelegate<OtlpHttpLogsExporterBehavior> = ExporterDelegate(),
) : Behavior<LogRecordExporterBehavior>, Exporter by delegate {
    constructor(
        console: ConsoleExporterBehavior? = null,
        http: OtlpHttpLogsExporterBehavior? = null,
    ) : this(ExporterDelegate(console = console, http = http))

    /**
     * Preserve the signal-specific HTTP exporter type, which would otherwise be widened to OtlpExporter by interface
     * delegation.
     */
    override val http: OtlpHttpLogsExporterBehavior?
        get() = delegate.http

    override fun mergeWith(higher: LogRecordExporterBehavior): LogRecordExporterBehavior = copy(
        delegate = delegate.copy(
            console = mergeNode(console, higher.console),
            http = mergeNode(http, higher.http),
        )
    )
}
