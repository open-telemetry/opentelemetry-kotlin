package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Selecting the OTLP HTTP Span exporter.
 *
 * https://opentelemetry.io/docs/specs/otel/protocol/exporter/
 */
@ExperimentalApi
data class OtlpHttpSpanExporterBehavior(
    val delegate: OtlpHttpExporter = OtlpHttpExporter()
) : Behavior<OtlpHttpSpanExporterBehavior>, OtlpExporter by delegate {
    constructor(
        endpoint: String? = null,
        timeout: Long? = null,
        headers: Map<String, String?>? = null,
    ) : this(OtlpHttpExporter(endpoint, timeout, headers))

    override fun mergeWith(higher: OtlpHttpSpanExporterBehavior): OtlpHttpSpanExporterBehavior {
        return copy(
            delegate = OtlpHttpExporter(
                endpoint = higher.endpoint ?: endpoint,
                timeout = higher.timeout ?: timeout,
                headers = mergeMap(headers, higher.headers),
            ),
        )
    }
}
