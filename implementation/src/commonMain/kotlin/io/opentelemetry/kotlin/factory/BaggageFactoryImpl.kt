package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageCreationAction
import io.opentelemetry.kotlin.baggage.BaggageEntry
import io.opentelemetry.kotlin.baggage.BaggageEntryImpl
import io.opentelemetry.kotlin.baggage.BaggageEntryMetadataImpl
import io.opentelemetry.kotlin.baggage.BaggageImpl

@OptIn(ExperimentalApi::class)
internal class BaggageFactoryImpl : BaggageFactory {

    override fun empty(): Baggage = BaggageImpl.EMPTY

    override fun create(action: BaggageCreationAction.() -> Unit): Baggage {
        val builder = BaggageCreationActionImpl()
        builder.action()
        return builder.build()
    }

    private class BaggageCreationActionImpl : BaggageCreationAction {

        private val entries: MutableMap<String, BaggageEntry> = mutableMapOf()

        override fun put(name: String, value: String, metadata: String) {
            entries[name] = BaggageEntryImpl(value, BaggageEntryMetadataImpl(metadata))
        }

        override fun remove(name: String) {
            entries.remove(name)
        }

        fun build(): Baggage = when {
            entries.isEmpty() -> BaggageImpl.EMPTY
            else -> BaggageImpl(entries.toMap())
        }
    }
}
