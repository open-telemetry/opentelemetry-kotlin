package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
internal object NoopBaggage : Baggage {
    override fun getValue(name: String): String? = null
    override fun asMap(): Map<String, BaggageEntry> = emptyMap()
    override fun set(name: String, value: String): Baggage = this
    override fun set(name: String, value: String, metadata: BaggageEntryMetadata): Baggage = this
    override fun remove(name: String): Baggage = this
}
