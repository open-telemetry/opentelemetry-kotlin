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
    val signalEndpoint: String?
        get() = delegate.signalEndpoint

    constructor(
        endpoint: String? = null,
        timeout: Long? = null,
        headers: Map<String, String?>? = null,
    ) : this(OtlpHttpExporter(endpoint = endpoint, timeout = timeout, headers = headers))

    constructor(
        endpoint: String? = null,
        signalEndpoint: String? = null,
        timeout: Long? = null,
        headers: Map<String, String?>? = null,
    ) : this(OtlpHttpExporter(endpoint, signalEndpoint, timeout, headers))

    override fun mergeWith(higher: OtlpHttpLogsExporterBehavior): OtlpHttpLogsExporterBehavior {
        return copy(
            delegate = OtlpHttpExporter(
                endpoint = higher.endpoint ?: endpoint,
                signalEndpoint = higher.signalEndpoint ?: signalEndpoint,
                timeout = higher.timeout ?: timeout,
                headers = mergeMap(headers, higher.headers),
            ),
        )
    }
}
