package io.opentelemetry.kotlin.baggage

class FakeBaggage(
    private val entries: Map<String, BaggageEntry> = emptyMap(),
) : Baggage {
    override fun getValue(name: String): String? = entries[name]?.value
    override fun asMap(): Map<String, BaggageEntry> = entries
    override fun set(name: String, value: String): Baggage = this
    override fun set(name: String, value: String, metadata: BaggageEntryMetadata): Baggage = this
    override fun remove(name: String): Baggage = this
}
