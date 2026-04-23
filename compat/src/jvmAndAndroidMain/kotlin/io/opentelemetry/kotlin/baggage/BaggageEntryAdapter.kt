package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntry

@OptIn(ExperimentalApi::class)
internal class BaggageEntryAdapter(
    private val impl: OtelJavaBaggageEntry
) : BaggageEntry {

    override val value: String get() = impl.value

    override val metadata: BaggageEntryMetadata get() = BaggageEntryMetadataAdapter(impl.metadata)
}
