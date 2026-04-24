package io.opentelemetry.kotlin.tracing.export

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.EXPORT_INITIAL_DELAY_MS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPTS
import io.opentelemetry.kotlin.export.EXPORT_MAX_ATTEMPT_INTERVAL_MS
import io.opentelemetry.kotlin.export.EXPORT_REQUEST_TIMEOUT_MS
import io.opentelemetry.kotlin.export.OtlpClient
import io.opentelemetry.kotlin.export.createDefaultHttpClient
import io.opentelemetry.kotlin.export.createHttpEngine
import io.opentelemetry.kotlin.init.TraceExportConfigDsl

/**
 * Creates a span exporter that sends telemetry to the specified URL over OTLP.
 */
@ExperimentalApi
public fun TraceExportConfigDsl.otlpHttpSpanExporter(
    baseUrl: String,
    httpClientEngine: HttpClientEngine = createHttpEngine(),
    timeoutMs: Long = EXPORT_REQUEST_TIMEOUT_MS,
): SpanExporter = OtlpHttpSpanExporter(
    OtlpClient(baseUrl, createDefaultHttpClient(requestTimeoutMs = timeoutMs, engine = httpClientEngine)),
    EXPORT_INITIAL_DELAY_MS,
    EXPORT_MAX_ATTEMPT_INTERVAL_MS,
    EXPORT_MAX_ATTEMPTS
)

/**
 * Creates a span exporter that sends telemetry to the specified URL over OTLP using a custom Ktor [HttpClient].
 *
 * Use this overload to supply a pre-configured client with custom authentication, interceptors, or certificates.
 *
 * It's strongly recommended that the supplied [HttpClient] installs the
 * [io.ktor.client.plugins.HttpTimeout],
 * [io.ktor.client.plugins.contentnegotiation.ContentNegotiation] and
 * [io.ktor.client.plugins.compression.ContentEncoding] plugins and that gzip compression is enabled.
 */
@ExperimentalApi
public fun TraceExportConfigDsl.otlpHttpSpanExporter(
    baseUrl: String,
    httpClient: HttpClient,
): SpanExporter = OtlpHttpSpanExporter(
    OtlpClient(baseUrl, httpClient),
    EXPORT_INITIAL_DELAY_MS,
    EXPORT_MAX_ATTEMPT_INTERVAL_MS,
    EXPORT_MAX_ATTEMPTS
)
