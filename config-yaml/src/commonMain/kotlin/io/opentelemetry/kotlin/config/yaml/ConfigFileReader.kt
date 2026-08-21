package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration

/**
 * Reads the declarative config file the SDK was pointed at.
 */
@ExperimentalApi
fun interface ConfigFileReader {

    /**
     * Reads and parses the declarative config file at [path].
     *
     * Throws if the file cannot be read, or does not hold a valid configuration.
     */
    fun read(path: String): OpenTelemetryConfiguration
}
