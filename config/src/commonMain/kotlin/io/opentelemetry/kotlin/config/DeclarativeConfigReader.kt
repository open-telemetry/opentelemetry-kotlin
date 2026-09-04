package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior

/**
 * Reads the declarative config file the SDK was pointed at.
 */
@ExperimentalApi
fun interface DeclarativeConfigReader {

    /**
     * Reads and parses the declarative config file at [path].
     *
     * Throws if the file cannot be read, or does not hold a valid configuration.
     */
    fun read(path: String): OpenTelemetryBehavior
}
