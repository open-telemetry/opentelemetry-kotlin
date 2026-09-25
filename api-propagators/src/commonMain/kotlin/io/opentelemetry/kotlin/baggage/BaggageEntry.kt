package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * An entry in [Baggage], consisting of a string value and optional [BaggageEntryMetadata].
 *
 * https://opentelemetry.io/docs/specs/otel/baggage/api/#baggageentry
 */
@ExperimentalApi
public interface BaggageEntry {

    /** The baggage entry value. */
    public val value: String

    /** Metadata associated with this entry. */
    public val metadata: BaggageEntryMetadata
}
