package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class BaggageImpl private constructor(
    private val entries: Map<String, BaggageEntry>,
) : Baggage {

    override fun getValue(name: String): String? = entries[name]?.value

    override fun asMap(): Map<String, BaggageEntry> = entries

    override fun put(name: String, value: String, metadata: String): Baggage {
        if (entries.size >= MAX_ENTRIES && name !in entries) {
            return this
        }
        val entryMetadata = when {
            metadata.isEmpty() -> EMPTY_METADATA
            else -> BaggageEntryMetadataImpl(metadata)
        }
        return BaggageImpl(entries + (name to BaggageEntryImpl(value, entryMetadata)))
    }

    override fun remove(name: String): Baggage =
        when (name) {
            !in entries -> this
            else -> BaggageImpl(entries - name)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is BaggageImpl) {
            return false
        }
        return entries == other.entries
    }

    override fun hashCode(): Int = entries.hashCode()

    override fun toString(): String = "BaggageImpl(entries=$entries)"

    companion object {
        const val MAX_ENTRIES = 180

        val EMPTY: Baggage = BaggageImpl(emptyMap())

        private val EMPTY_METADATA = BaggageEntryMetadataImpl("")
    }
}
