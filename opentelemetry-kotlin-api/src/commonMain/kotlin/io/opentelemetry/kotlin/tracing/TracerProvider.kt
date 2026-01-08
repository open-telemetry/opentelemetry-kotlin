package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer

/**
 * TracerProvider is a factory for retrieving instances of [Tracer].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#tracerprovider
 */
@ExperimentalApi
@ThreadSafe
public interface TracerProvider {

    /**
     * Returns a [Tracer] matching the given name. An optional version, schema URL, and attributes describing
     * the [Tracer] can be supplied.
     *
     * The name must document the instrumentation scope: https://opentelemetry.io/docs/specs/otel/glossary/#instrumentation-scope
     */
    @ThreadSafe
    public fun getTracer(
        name: String,
        version: String? = null,
        schemaUrl: String? = null,
        attributes: (MutableAttributeContainer.() -> Unit)? = null,
    ): Tracer
}
