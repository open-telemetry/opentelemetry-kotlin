
package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.otlpHttpLogRecordExporter
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.stdoutLogRecordExporter
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.stdoutSpanExporter

/**
 * Creates a [SpanProcessor]. If a URL is supplied via [AppConfig.url] an OTLP HTTP exporter will
 * be used with batch processing. Otherwise, telemetry will be printed to stdout immediately.
 */
@OptIn(ExperimentalApi::class)
internal fun TraceExportConfigDsl.createSpanProcessor(url: String?): SpanProcessor = when (url) {
    null -> simpleSpanProcessor(stdoutSpanExporter())
    else -> batchSpanProcessor(otlpHttpSpanExporter(url))
}

/**
 * Creates a [LogRecordProcessor]. If a URL is supplied via [AppConfig.url] an OTLP HTTP exporter
 * will be used with batch processing. Otherwise, telemetry will be printed to stdout immediately.
 */
@OptIn(ExperimentalApi::class)
internal fun LogExportConfigDsl.createLogRecordProcessor(url: String?): LogRecordProcessor = when (url) {
    null -> simpleLogRecordProcessor(stdoutLogRecordExporter())
    else -> batchLogRecordProcessor(otlpHttpLogRecordExporter(url))
}

