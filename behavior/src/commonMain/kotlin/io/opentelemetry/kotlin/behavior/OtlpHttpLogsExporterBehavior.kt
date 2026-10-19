package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Selecting the OTLP HTTP Logs exporter.
 *
 * https://opentelemetry.io/docs/specs/otel/protocol/exporter/
 */
@ExperimentalApi
data class OtlpHttpLogsExporterBehavior(
    val delegate: OtlpHttpExporter = OtlpHttpExporter()
) : Behavior<OtlpHttpLogsExporterBehavior>, OtlpExporter by delegate {
    constructor(
        endpoint: String? = null,
        timeout: Long? = null,
        headers: Map<String, String?>? = null,
    ) : this(OtlpHttpExporter(endpoint, timeout, headers))

    override fun mergeWith(higher: OtlpHttpLogsExporterBehavior): OtlpHttpLogsExporterBehavior {
        return copy(
            delegate = OtlpHttpExporter(
                endpoint = higher.endpoint ?: endpoint,
                timeout = higher.timeout ?: timeout,
                headers = mergeMap(headers, higher.headers),
            ),
        )
    }
}
