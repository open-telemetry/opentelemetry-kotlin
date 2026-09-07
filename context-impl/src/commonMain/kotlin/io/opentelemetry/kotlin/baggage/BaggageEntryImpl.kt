package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal data class BaggageEntryImpl(
    override val value: String,
    override val metadata: BaggageEntryMetadata,
) : BaggageEntry
