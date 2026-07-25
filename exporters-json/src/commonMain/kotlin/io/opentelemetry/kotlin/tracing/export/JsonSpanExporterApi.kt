
package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.TraceExportConfigDsl

/**
 * Creates an in-memory log record exporter that stores telemetry in memory.
 * This is intended for development/testing rather than production use.
 */
@ExperimentalApi
public fun TraceExportConfigDsl.jsonSpanExporter(): JsonSpanExporter =
    JsonSpanExporterImpl()
