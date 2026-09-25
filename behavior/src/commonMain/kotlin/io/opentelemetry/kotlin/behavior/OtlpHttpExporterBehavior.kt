package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
/**
 * Selecting the OTLP HTTP exporter.
 *
 * https://opentelemetry.io/docs/specs/otel/protocol/exporter/
 */
data class OtlpHttpExporterBehavior(
    // TODO: Add all fields supported by the spec.
    //  Blocked by #974. (OTLP exporter configuration surface)
    /**
     * Target to which the exporter is going to send spans, metrics, or logs.
     */
    val endpoint: String? = null,
    /**
     * Maximum time (in milliseconds) to wait for each export.
     */
    val timeout: Long? = null,
) : Behavior<OtlpHttpExporterBehavior> {
    override fun mergeWith(higher: OtlpHttpExporterBehavior): OtlpHttpExporterBehavior {
        return copy(
            endpoint = higher.endpoint ?: endpoint,
            timeout = higher.timeout ?: timeout,
        )
    }
}
