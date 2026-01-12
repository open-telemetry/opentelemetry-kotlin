package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer

/**
 * Provider for retrieving [Logger] instances.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/api/#loggerprovider
 */
@ExperimentalApi
@ThreadSafe
public interface LoggerProvider {

    /**
     * Returns a [Logger] matching the given name. An optional version, schema URL, and attributes describing
     * the [Logger] can be supplied.
     *
     * The name must document the instrumentation scope: https://opentelemetry.io/docs/specs/otel/glossary/#instrumentation-scope
     */
    @ThreadSafe
    public fun getLogger(
        name: String,
        version: String? = null,
        schemaUrl: String? = null,
        attributes: (MutableAttributeContainer.() -> Unit)? = null,
    ): Logger
}
