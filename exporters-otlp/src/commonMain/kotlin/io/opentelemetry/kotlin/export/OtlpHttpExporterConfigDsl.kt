package io.opentelemetry.kotlin.export

import io.ktor.client.engine.HttpClientEngine
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.init.ConfigDsl

internal const val DEFAULT_OTLP_HTTP_ENDPOINT = "http://localhost:4318"

/**
 * Configures an OTLP HTTP exporter.
 */
@ExperimentalApi
@ConfigDsl
public class OtlpHttpExporterConfigDsl internal constructor() {

    /**
     * Collector base URL. Defaults to `http://localhost:4318`.
     */
    public var endpoint: String = DEFAULT_OTLP_HTTP_ENDPOINT

    /**
     * HTTP request timeout in milliseconds. Defaults to 10 seconds.
     */
    public var timeoutMs: Long = EXPORT_REQUEST_TIMEOUT_MS

    internal val headers: MutableMap<String, String> = linkedMapOf()
    internal var httpClientEngine: HttpClientEngine? = null

    /**
     * Adds a header to every export request. A later call with the same [name] replaces the previous value.
     * Blank names are ignored.
     */
    public fun header(name: String, value: String) {
        if (name.isBlank()) {
            return
        }
        headers[name] = value
    }
}

internal fun createOtlpHttpClient(
    sdkErrorHandler: SdkErrorHandler,
    block: OtlpHttpExporterConfigDsl.() -> Unit,
): OtlpClient {
    val config = OtlpHttpExporterConfigDsl().apply(block)
    return OtlpClient(
        baseUrl = config.endpoint,
        httpClient = HttpClientRegistry.getOrCreate(
            engine = config.httpClientEngine ?: createHttpEngine(),
            requestTimeoutMs = config.timeoutMs,
        ),
        sdkErrorHandler = sdkErrorHandler,
        requestHeaders = config.headers.toMap(),
    )
}
