package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntryMetadata

@OptIn(ExperimentalApi::class)
internal class BaggageEntryMetadataAdapter(
    private val impl: OtelJavaBaggageEntryMetadata
) : BaggageEntryMetadata {

    override val value: String get() = impl.value
}
