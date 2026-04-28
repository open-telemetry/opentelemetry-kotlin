package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class BaggageImpl private constructor(
    private val entries: Map<String, BaggageEntry>,
) : Baggage {

    override fun getValue(name: String): String? = entries[name]?.value

    override fun asMap(): Map<String, BaggageEntry> = entries

    override fun set(name: String, value: String): Baggage =
        BaggageImpl(entries + (name to BaggageEntryImpl(value, EMPTY_METADATA)))

    override fun set(name: String, value: String, metadata: BaggageEntryMetadata): Baggage =
        BaggageImpl(entries + (name to BaggageEntryImpl(value, metadata)))

    override fun remove(name: String): Baggage =
        when (name) {
            !in entries -> this
            else -> BaggageImpl(entries - name)
        }

    companion object {
        val EMPTY: Baggage = BaggageImpl(emptyMap())
        private val EMPTY_METADATA = BaggageEntryMetadataImpl("")
    }
}
