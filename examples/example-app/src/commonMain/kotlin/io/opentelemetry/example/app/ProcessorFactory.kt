package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.ExportConfigDsl
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.otlpHttpLogRecordExporter
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter

/**
 * Creates a [SpanProcessor] that batches spans and exports them to [url] via OTLP/HTTP.
 */
@OptIn(ExperimentalApi::class)
internal fun ExportConfigDsl.createSpanProcessor(url: String): SpanProcessor =
    batchSpanProcessor(otlpHttpSpanExporter { endpoint = url })

/**
 * Creates a [LogRecordProcessor] that batches logs and exports them to [url] via OTLP/HTTP.
 */
@OptIn(ExperimentalApi::class)
internal fun ExportConfigDsl.createLogRecordProcessor(url: String): LogRecordProcessor =
    batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = url })
