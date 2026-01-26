@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates an in-memory log record exporter that stores telemetry in memory.
 * This is intended for development/testing rather than production use.
 */
@ExperimentalApi
public fun createInMemoryLogRecordExporter(): InMemoryLogRecordExporter =
    InMemoryLogRecordExporterImpl()
