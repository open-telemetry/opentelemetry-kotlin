package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
public data class BaggageEntryMetadataImpl(override val value: String) : BaggageEntryMetadata
