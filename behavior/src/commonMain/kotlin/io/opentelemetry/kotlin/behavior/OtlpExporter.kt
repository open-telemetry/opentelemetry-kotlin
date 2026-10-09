package io.opentelemetry.kotlin.behavior

interface OtlpExporter {
    /**
     * Target to which the exporter is going to send spans, metrics, or logs.
     *
     * When used as the generic OTLP endpoint, the exporter appends the
     * signal-specific path (`/v1/traces`, `/v1/metrics`, or `/v1/logs`).
     */
    val endpoint: String

    /**
     * Maximum time (in milliseconds) to wait for each export.
     */
    val timeout: Long

    /**
     * Configure headers.
     */
    val headers: Map<String, String?>?

    companion object {
        const val DEFAULT_TIMEOUT = 10_000L

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
