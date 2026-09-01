package io.opentelemetry.kotlin.export

import io.ktor.client.HttpClient
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
public interface OtlpHttpExporterConfigDsl {

    /**
     * Collector base URL. Defaults to `http://localhost:4318`.
     */
    public var endpoint: String

    /**
     * HTTP request timeout in milliseconds. Defaults to 10 seconds.
     * Ignored when [httpClient] is set.
     */
    public var timeoutMs: Long

    /**
     * Optional Ktor engine. When unset, exporters share a default engine.
     * Ignored when [httpClient] is set.
     */
    public var httpClientEngine: HttpClientEngine?

    /**
     * Optional pre-configured Ktor [HttpClient]. Use this to supply custom authentication,
     * interceptors, or certificates.
     *
     * It's strongly recommended that the supplied [HttpClient] installs the
     * [io.ktor.client.plugins.HttpTimeout],
     * [io.ktor.client.plugins.contentnegotiation.ContentNegotiation] and
     * [io.ktor.client.plugins.compression.ContentEncoding] plugins and that gzip compression is enabled.
     */
    public var httpClient: HttpClient?
}

internal class OtlpHttpExporterConfig : OtlpHttpExporterConfigDsl {
    override var endpoint: String = DEFAULT_OTLP_HTTP_ENDPOINT
    override var timeoutMs: Long = EXPORT_REQUEST_TIMEOUT_MS
    override var httpClientEngine: HttpClientEngine? = null
    override var httpClient: HttpClient? = null
}

internal fun createOtlpHttpClient(
    sdkErrorHandler: SdkErrorHandler,
    block: OtlpHttpExporterConfigDsl.() -> Unit,
): OtlpClient {
    val config = OtlpHttpExporterConfig().apply(block)
    val httpClient = config.httpClient ?: HttpClientRegistry.getOrCreate(
        engine = config.httpClientEngine,
        requestTimeoutMs = config.timeoutMs,
    )
    return OtlpClient(
        baseUrl = config.endpoint,
        httpClient = httpClient,
        sdkErrorHandler = sdkErrorHandler,
    )
}
