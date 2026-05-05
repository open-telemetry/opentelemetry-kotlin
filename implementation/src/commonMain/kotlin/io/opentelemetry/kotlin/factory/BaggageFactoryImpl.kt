package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageCreationAction
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

        private var baggage: Baggage = BaggageImpl.EMPTY

        override fun put(name: String, value: String, metadata: String) {
            baggage = baggage.set(name, value, BaggageEntryMetadataImpl(metadata))
        }

        override fun remove(name: String) {
            baggage = baggage.remove(name)
        }

        fun build(): Baggage = baggage
    }
}
