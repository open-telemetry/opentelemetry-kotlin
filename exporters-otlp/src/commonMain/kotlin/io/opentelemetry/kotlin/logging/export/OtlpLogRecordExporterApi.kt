package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.EXPORT_INITIAL_DELAY_MS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPTS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPT_INTERVAL_MS
import io.opentelemetry.kotlin.export.OtlpEndpoint
import io.opentelemetry.kotlin.export.OtlpFileExporterConfigDsl
import io.opentelemetry.kotlin.export.OtlpHttpExporterConfigDsl
import io.opentelemetry.kotlin.export.createOtlpFileExporter
import io.opentelemetry.kotlin.export.createOtlpHttpClient
import io.opentelemetry.kotlin.init.ExportConfigDsl

/**
 * Creates a log record exporter that sends telemetry over OTLP HTTP.
 */
@ExperimentalApi
public fun ExportConfigDsl.otlpHttpLogRecordExporter(
    block: OtlpHttpExporterConfigDsl.() -> Unit = {},
): LogRecordExporter =
    OtlpHttpLogRecordExporter(
        createOtlpHttpClient(sdkErrorHandler, OtlpEndpoint.Logs, block = block),
        EXPORT_INITIAL_DELAY_MS,
        EXPORT_MAX_ATTEMPT_INTERVAL_MS,
        EXPORT_MAX_ATTEMPTS,
        sdkErrorHandler,
    )

/**
 * Creates a log record exporter that writes telemetry to a file.
 */
@ExperimentalApi
internal fun ExportConfigDsl.otlpFileLogRecordExporter(
    block: OtlpFileExporterConfigDsl.() -> Unit
): LogRecordExporter =
    OtlpFileLogRecordExporter(
        fileExporter = createOtlpFileExporter(block),
        initialDelayMs = EXPORT_INITIAL_DELAY_MS,
        maxAttemptIntervalMs = EXPORT_MAX_ATTEMPT_INTERVAL_MS,
        maxAttempts = EXPORT_MAX_ATTEMPTS,
        sdkErrorHandler = sdkErrorHandler
    )
