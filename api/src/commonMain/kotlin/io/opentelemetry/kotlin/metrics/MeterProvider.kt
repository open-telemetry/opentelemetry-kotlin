package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * MeterProvider is a factory for retrieving instances of [Meter].
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#meterprovider
 */
@ExperimentalApi
@ThreadSafe
public interface MeterProvider {

    /**
     * Returns a [Meter] matching the given name. An optional version, schema URL, and attributes describing
     * the [Meter] can be supplied.
     *
     * The name must document the instrumentation scope: https://opentelemetry.io/docs/specs/otel/glossary/#instrumentation-scope
     */
    @ThreadSafe
    public fun getMeter(
        name: String,
        version: String? = null,
        schemaUrl: String? = null,
        attributes: (AttributesMutator.() -> Unit)? = null,
    ): Meter
}
