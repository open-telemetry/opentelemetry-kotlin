
package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.TraceExportConfigDsl

/**
 * Exports trace as OTLP JSON
 */
@ExperimentalApi
public fun TraceExportConfigDsl.jsonSpanExporter(): JsonSpanExporter =
    JsonSpanExporterImpl()
