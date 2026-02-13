@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.EXPORT_INITIAL_DELAY_MS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPTS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPT_INTERVAL_MS
import io.opentelemetry.kotlin.export.OtlpClient

/**
 * Creates a log record exporter that sends telemetry to the specified URL over OTLP.
 */
@ExperimentalApi
public fun createOtlpHttpLogRecordExporter(baseUrl: String): LogRecordExporter =
    OtlpHttpLogRecordExporter(
        OtlpClient(baseUrl),
        EXPORT_INITIAL_DELAY_MS,
        EXPORT_MAX_ATTEMPT_INTERVAL_MS,
        EXPORT_MAX_ATTEMPTS
    )
