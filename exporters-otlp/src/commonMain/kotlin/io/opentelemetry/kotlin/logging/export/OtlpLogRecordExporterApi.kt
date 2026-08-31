package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.EXPORT_INITIAL_DELAY_MS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPTS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPT_INTERVAL_MS
import io.opentelemetry.kotlin.export.OtlpHttpExporterConfigDsl
import io.opentelemetry.kotlin.export.createOtlpHttpClient
import io.opentelemetry.kotlin.init.LogExportConfigDsl

/**
 * Creates a log record exporter that sends telemetry over OTLP HTTP.
 */
@ExperimentalApi
public fun LogExportConfigDsl.otlpHttpLogRecordExporter(
    block: OtlpHttpExporterConfigDsl.() -> Unit = {},
): LogRecordExporter =
    OtlpHttpLogRecordExporter(
        createOtlpHttpClient(sdkErrorHandler, block),
        EXPORT_INITIAL_DELAY_MS,
        EXPORT_MAX_ATTEMPT_INTERVAL_MS,
        EXPORT_MAX_ATTEMPTS,
        sdkErrorHandler,
    )
