package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * OTLP exporter.
 *
 * https://opentelemetry.io/docs/specs/otel/protocol/exporter/
 */
@ExperimentalApi
interface OtlpExporter {
    // TODO: Add all fields supported by the spec.
    //  Blocked by #974. (OTLP exporter configuration surface)
    /**
     * Target to which the exporter is going to send spans, metrics, or logs.
     */
    val endpoint: String?

    /**
     * Maximum time (in milliseconds) to wait for each export.
     */
    val timeout: Long?

    /**
     * Configure headers.
     */
    val headers: Map<String, String?>?

    companion object {
        fun buildHeaderMap(headerString: String?): Map<String, String>? {
            headerString ?: return null
            return buildMap {
                headerString.split(",").forEach { header ->
                    // Trailing and leading whitespaces are allowed but not considered part of key/value.
                    // See https://www.w3.org/TR/baggage/#key and https://www.w3.org/TR/baggage/#value
                    val parts = header.split("=", limit = 2)
                    if (parts.size != 2) {
                        return@forEach
                    }
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        put(key, value)
                    }
                }
            }
        }
    }
}
