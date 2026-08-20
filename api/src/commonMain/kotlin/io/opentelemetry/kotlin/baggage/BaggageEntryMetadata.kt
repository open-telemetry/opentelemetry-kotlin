package io.opentelemetry.kotlin.baggage

/**
 * Opaque metadata associated with a [BaggageEntry].
 *
 * The API layer assigns no semantic meaning to the metadata value; it is used
 * exclusively by [TextMapPropagator][io.opentelemetry.kotlin.propagation.TextMapPropagator]
 * implementations to encode and decode propagation-format-specific data.
 *
 * https://opentelemetry.io/docs/specs/otel/baggage/api/#baggageentrymetadata
 */
public interface BaggageEntryMetadata {

    /** The raw metadata string. */
    public val value: String
}
