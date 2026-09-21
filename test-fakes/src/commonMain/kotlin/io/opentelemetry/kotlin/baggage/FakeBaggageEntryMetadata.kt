package io.opentelemetry.kotlin.baggage

data class FakeBaggageEntryMetadata(override val value: String = "") : BaggageEntryMetadata
