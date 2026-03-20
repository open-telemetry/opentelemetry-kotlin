
package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.LogExportConfigDsl

/**
 * Creates an in-memory log record exporter that stores telemetry in memory.
 * This is intended for development/testing rather than production use.
 */
@ExperimentalApi
public fun LogExportConfigDsl.inMemoryLogRecordExporter(): InMemoryLogRecordExporter =
    InMemoryLogRecordExporterImpl()
