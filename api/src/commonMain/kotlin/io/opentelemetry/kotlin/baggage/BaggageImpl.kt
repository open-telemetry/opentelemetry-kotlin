package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.AtomicBoolean
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

@OptIn(ExperimentalApi::class)
internal class BaggageImpl private constructor(
    private val entries: Map<String, BaggageEntry>,
) : Baggage {

    override fun getValue(name: String): String? = entries[name]?.value

    override fun asMap(): Map<String, BaggageEntry> = entries

    override fun put(name: String, value: String, metadata: String): Baggage {
        if (entries.size >= MAX_ENTRIES && name !in entries) {
            reportEntryLimitReached()
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

    /**
     * Reports the first [put] that is dropped because the baggage is full. Later drops are not
     * reported, so code that keeps adding names to a full baggage can't flood the error handler.
     */
    private fun reportEntryLimitReached() {
        if (!entryLimitReported.compareAndSet(expect = false, update = true)) {
            return
        }
        reportError(
            SdkError.ApiMisuse(
                api = "Baggage.put",
                message = "Baggage already has $MAX_ENTRIES entries, so a new name was dropped. " +
                    "Later dropped names are not reported.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }

    companion object {
        const val MAX_ENTRIES = 180

        /**
         * Whether a [put] dropped because of [MAX_ENTRIES] has been reported in this process.
         */
        internal val entryLimitReported = AtomicBoolean(false)

        val EMPTY: Baggage = BaggageImpl(emptyMap())

        private val EMPTY_METADATA = BaggageEntryMetadataImpl("")
    }
}
