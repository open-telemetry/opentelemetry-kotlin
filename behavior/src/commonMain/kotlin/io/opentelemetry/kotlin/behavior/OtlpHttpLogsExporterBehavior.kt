package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Selecting the OTLP HTTP logs exporter.
 *
 *
 * https://opentelemetry.io/docs/specs/otel/protocol/exporter/
 */
@ExperimentalApi
data class OtlpHttpLogsExporterBehavior(
    override val endpoint: String = DEFAULT_ENDPOINT,
    override val timeout: Long = DEFAULT_TIMEOUT,
    override val headers: Map<String, String?>? = null
) : OtlpHttpExporterBehavior(endpoint, timeout, headers) {
    override fun mergeWith(higher: OtlpHttpExporterBehavior): OtlpHttpLogsExporterBehavior {
        return copy(
            endpoint = higher.endpoint,
            timeout = higher.timeout,
            headers = mergeMap(headers, higher.headers),
        )
    }

    companion object {
        const val DEFAULT_ENDPOINT = "http://localhost:4318/v1/logs"
    }
}
