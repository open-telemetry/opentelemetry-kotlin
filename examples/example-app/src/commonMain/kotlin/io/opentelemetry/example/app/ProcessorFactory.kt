@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.createBatchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.createOtlpHttpLogRecordExporter
import io.opentelemetry.kotlin.logging.export.createSimpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.createStdoutLogRecordExporter
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.export.createBatchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.createOtlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.export.createSimpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.createStdoutSpanExporter

/**
 * Creates a [SpanProcessor]. If a URL is supplied via [AppConfig.url] an OTLP HTTP exporter will
 * be used with batch processing. Otherwise, telemetry will be printed to stdout immediately.
 */
@Suppress("DEPRECATION")
internal fun createSpanProcessor(url: String?): SpanProcessor = when (url) {
    null -> createSimpleSpanProcessor(createStdoutSpanExporter())
    else -> createBatchSpanProcessor(createOtlpHttpSpanExporter(url))
}

/**
 * Creates a [LogRecordProcessor]. If a URL is supplied via [AppConfig.url] an OTLP HTTP exporter
 * will be used with batch processing. Otherwise, telemetry will be printed to stdout immediately.
 */
@Suppress("DEPRECATION")
internal fun createLogRecordProcessor(url: String?): LogRecordProcessor = when (url) {
    null -> createSimpleLogRecordProcessor(createStdoutLogRecordExporter())
    else -> createBatchLogRecordProcessor(createOtlpHttpLogRecordExporter(url))
}

