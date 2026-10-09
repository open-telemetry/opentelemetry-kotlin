package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OtlpExporter.Companion.DEFAULT_TIMEOUT

/**
 * Selecting the OTLP HTTP span exporter.
 *
 * https://opentelemetry.io/docs/specs/otel/protocol/exporter/
 */
@ExperimentalApi
data class OtlpHttpSpanExporterBehavior(
    val delegate: OtlpHttpExporter = OtlpHttpExporter(
        endpoint = DEFAULT_ENDPOINT,
    ),
) : Behavior<OtlpHttpSpanExporterBehavior>, OtlpExporter by delegate {
    constructor(
        endpoint: String = DEFAULT_ENDPOINT,
        timeout: Long = DEFAULT_TIMEOUT,
        headers: Map<String, String?>? = null,
    ) : this(
        delegate = OtlpHttpExporter(
            endpoint = endpoint,
            timeout = timeout,
            headers = headers,
        )
    )

    override fun mergeWith(higher: OtlpHttpSpanExporterBehavior): OtlpHttpSpanExporterBehavior {
        return copy(
            delegate = OtlpHttpExporter(
                endpoint = higher.endpoint,
                timeout = higher.timeout,
                headers = mergeMap(headers, higher.headers),
            )
        )
    }

    companion object {
        const val DEFAULT_ENDPOINT = "http://localhost:4318/v1/traces"
    }
}
