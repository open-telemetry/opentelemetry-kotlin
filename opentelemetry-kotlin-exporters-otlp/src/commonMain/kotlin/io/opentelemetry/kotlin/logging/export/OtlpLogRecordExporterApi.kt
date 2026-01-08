@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates a log record exporter that sends telemetry to the specified URL over OTLP.
 */
@ExperimentalApi
public expect fun createOtlpHttpLogRecordExporter(baseUrl: String): LogRecordExporter
