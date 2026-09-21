package io.opentelemetry.kotlin.baggage

data class FakeBaggageEntry(
    override val value: String,
    override val metadata: BaggageEntryMetadata = FakeBaggageEntryMetadata(),
) : BaggageEntry
