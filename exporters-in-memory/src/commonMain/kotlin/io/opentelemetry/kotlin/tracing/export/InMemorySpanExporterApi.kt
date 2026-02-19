@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.ConfigDsl
import io.opentelemetry.kotlin.init.TraceExportConfigDsl

/**
 * Creates an in-memory span exporter that stores telemetry in memory.
 * This is intended for development/testing rather than production use.
 */
@ExperimentalApi
@ConfigDsl
public fun TraceExportConfigDsl.inMemorySpanExporter(): InMemorySpanExporter {
    @Suppress("DEPRECATION")
    return createInMemorySpanExporter()
}

@ExperimentalApi
@Deprecated("Deprecated.", ReplaceWith("inMemorySpanExporter()"))
public fun createInMemorySpanExporter(): InMemorySpanExporter = InMemorySpanExporterImpl()
