package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal data class BaggageEntryMetadataImpl(override val value: String) : BaggageEntryMetadata
