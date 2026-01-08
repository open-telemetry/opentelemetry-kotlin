package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordExporter

/**
 * Converts an opentelemetry-java log record exporter to an opentelemetry-kotlin log record exporter.
 * This is useful if you wish to use an existing Java exporter whilst using opentelemetry-kotlin.
 */
@OptIn(ExperimentalApi::class)
public fun OtelJavaLogRecordExporter.toOtelKotlinLogRecordExporter(): LogRecordExporter =
    LogRecordExporterAdapter(this)
