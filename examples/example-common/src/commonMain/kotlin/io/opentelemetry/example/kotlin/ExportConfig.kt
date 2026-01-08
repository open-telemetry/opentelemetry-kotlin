@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.example.kotlin

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

/**
 * Creates a [SpanProcessor]. If a URL is supplied and the platform is the JVM/Android, an OTLP
 * HTTP exporter will be created - otherwise, telemetry will be printed to stdout.
 */
expect fun createSpanProcessor(url: String?): SpanProcessor

/**
 * Creates a [LogRecordProcessor]. If a URL is supplied and the platform is the JVM/Android, an OTLP
 * HTTP exporter will be created - otherwise, telemetry will be printed to stdout.
 */
expect fun createLogRecordProcessor(url: String?): LogRecordProcessor
