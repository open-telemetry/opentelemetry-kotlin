package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Selecting the OTLP HTTP exporter.
 *
 * https://opentelemetry.io/docs/specs/otel/protocol/exporter/
 */
@ExperimentalApi
data class OtlpHttpExporter(
    override val endpoint: String,
    override val timeout: Long = OtlpExporter.DEFAULT_TIMEOUT,
    override val headers: Map<String, String?>? = null,
) : OtlpExporter
