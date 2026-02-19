@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.ConfigDsl
import io.opentelemetry.kotlin.init.LogExportConfigDsl

/**
 * Creates an in-memory log record exporter that stores telemetry in memory.
 * This is intended for development/testing rather than production use.
 */
@ExperimentalApi
@ConfigDsl
public fun LogExportConfigDsl.inMemoryLogRecordExporter(): InMemoryLogRecordExporter {
    @Suppress("DEPRECATION")
    return createInMemoryLogRecordExporter()
}

@ExperimentalApi
@Deprecated("Deprecated.", ReplaceWith("inMemoryLogRecordExporter()"))
public fun createInMemoryLogRecordExporter(): InMemoryLogRecordExporter =
    InMemoryLogRecordExporterImpl()
