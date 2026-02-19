@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.logging.export

import io.ktor.client.engine.HttpClientEngine
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.EXPORT_INITIAL_DELAY_MS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPTS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPT_INTERVAL_MS
import io.opentelemetry.kotlin.export.OtlpClient
import io.opentelemetry.kotlin.export.createDefaultHttpClient
import io.opentelemetry.kotlin.export.createHttpEngine
import io.opentelemetry.kotlin.init.ConfigDsl
import io.opentelemetry.kotlin.init.LogExportConfigDsl

/**
 * Creates a log record exporter that sends telemetry to the specified URL over OTLP.
 */
@ExperimentalApi
@ConfigDsl
public fun LogExportConfigDsl.otlpHttpLogRecordExporter(
    baseUrl: String,
    httpClientEngine: HttpClientEngine = createHttpEngine(),
): LogRecordExporter {
    @Suppress("DEPRECATION")
    return createOtlpHttpLogRecordExporter(baseUrl, httpClientEngine)
}

@ExperimentalApi
@Deprecated("Deprecated.", ReplaceWith("otlpHttpLogRecordExporter(baseUrl, httpClientEngine)"))
public fun createOtlpHttpLogRecordExporter(
    baseUrl: String,
    httpClientEngine: HttpClientEngine = createHttpEngine(),
): LogRecordExporter =
    OtlpHttpLogRecordExporter(
        OtlpClient(baseUrl, createDefaultHttpClient(engine = httpClientEngine)),
        EXPORT_INITIAL_DELAY_MS,
        EXPORT_MAX_ATTEMPT_INTERVAL_MS,
        EXPORT_MAX_ATTEMPTS
    )
