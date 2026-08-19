
package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.LogExportConfigDsl

/**
 * Exports log records as OTLP JSON
 */
@ExperimentalApi
public fun LogExportConfigDsl.jsonLogRecordExporter(): JsonLogRecordExporter =
    JsonLogRecordExporterImpl()
