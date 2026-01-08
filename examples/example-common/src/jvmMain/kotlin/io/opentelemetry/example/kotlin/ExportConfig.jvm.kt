@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.example.kotlin

import io.opentelemetry.example.ExampleLogRecordProcessor
import io.opentelemetry.example.ExampleSpanProcessor
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.createOtlpHttpLogRecordExporter
import io.opentelemetry.kotlin.logging.export.createSimpleLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.export.createOtlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.export.createSimpleSpanProcessor

actual fun createSpanProcessor(url: String?): SpanProcessor = when (url) {
    null -> ExampleSpanProcessor()
    else -> createSimpleSpanProcessor(
        createOtlpHttpSpanExporter(url)
    )
}

actual fun createLogRecordProcessor(url: String?): LogRecordProcessor = when (url) {
    null -> ExampleLogRecordProcessor()
    else -> createSimpleLogRecordProcessor(
        createOtlpHttpLogRecordExporter(url)
    )
}
