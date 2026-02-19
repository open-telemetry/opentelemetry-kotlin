package io.opentelemetry.kotlin.tracing.export

import io.ktor.client.engine.HttpClientEngine
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.EXPORT_INITIAL_DELAY_MS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPTS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPT_INTERVAL_MS
import io.opentelemetry.kotlin.export.OtlpClient
import io.opentelemetry.kotlin.export.createDefaultHttpClient
import io.opentelemetry.kotlin.export.createHttpEngine
import io.opentelemetry.kotlin.init.ConfigDsl
import io.opentelemetry.kotlin.init.TraceExportConfigDsl

/**
 * Creates a span exporter that sends telemetry to the specified URL over OTLP.
 */
@ExperimentalApi
@ConfigDsl
public fun TraceExportConfigDsl.otlpHttpSpanExporter(
    baseUrl: String,
    httpClientEngine: HttpClientEngine = createHttpEngine(),
): SpanExporter {
    @Suppress("DEPRECATION")
    return createOtlpHttpSpanExporter(baseUrl, httpClientEngine)
}

@ExperimentalApi
@Deprecated("Deprecated.", ReplaceWith("otlpHttpSpanExporter(baseUrl, httpClientEngine)"))
public fun createOtlpHttpSpanExporter(
    baseUrl: String,
    httpClientEngine: HttpClientEngine = createHttpEngine(),
): SpanExporter = OtlpHttpSpanExporter(
    OtlpClient(baseUrl, createDefaultHttpClient(engine = httpClientEngine)),
    EXPORT_INITIAL_DELAY_MS,
    EXPORT_MAX_ATTEMPT_INTERVAL_MS,
    EXPORT_MAX_ATTEMPTS
)
