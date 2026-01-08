@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.example.kotlin

import io.opentelemetry.example.ExampleLogRecordProcessor
import io.opentelemetry.example.ExampleSpanProcessor
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

actual fun createSpanProcessor(url: String?): SpanProcessor = ExampleSpanProcessor()

actual fun createLogRecordProcessor(url: String?): LogRecordProcessor = ExampleLogRecordProcessor()
