package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.utils.isValidBaggageKey
import io.opentelemetry.kotlin.propagation.utils.isValidBaggageValue

@OptIn(ExperimentalApi::class)
internal class BaggageImpl private constructor(
    private val entries: Map<String, BaggageEntry>,
) : Baggage {

    override fun getValue(name: String): String? = entries[name]?.value

    override fun asMap(): Map<String, BaggageEntry> = entries

    override fun set(name: String, value: String): Baggage =
        setImpl(name, value, EMPTY_METADATA)

    override fun set(name: String, value: String, metadata: BaggageEntryMetadata): Baggage =
        setImpl(name, value, metadata)

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

    private fun setImpl(name: String, value: String, metadata: BaggageEntryMetadata): Baggage {
        if (!isValidBaggageKey(name)) {
            return this
        }
        if (!isValidBaggageValue(value)) {
            return this
        }
        if (entries.size >= MAX_ENTRIES && name !in entries) {
            return this
        }
        return BaggageImpl(entries + (name to BaggageEntryImpl(value, metadata)))
    }

    companion object {
        const val MAX_ENTRIES = 180

        val EMPTY: Baggage = BaggageImpl(emptyMap())

        private val EMPTY_METADATA = BaggageEntryMetadataImpl("")
    }
}
