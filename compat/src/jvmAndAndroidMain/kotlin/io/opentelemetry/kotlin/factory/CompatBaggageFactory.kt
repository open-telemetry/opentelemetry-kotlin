package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntryMetadata
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageAdapter
import io.opentelemetry.kotlin.baggage.BaggageCreationAction

@OptIn(ExperimentalApi::class)
internal class CompatBaggageFactory : BaggageFactory {

    override fun empty(): Baggage = BaggageAdapter(OtelJavaBaggage.empty())

    override fun create(action: BaggageCreationAction.() -> Unit): Baggage {
        val builder = OtelJavaBaggage.builder()
        BaggageCreationActionAdapter(builder).action()
        return BaggageAdapter(builder.build())
    }

    private class BaggageCreationActionAdapter(
        private val builder: OtelJavaBaggageBuilder,
    ) : BaggageCreationAction {

        override fun put(name: String, value: String, metadata: String) {
            builder.put(name, value, OtelJavaBaggageEntryMetadata.create(metadata))
        }

        override fun remove(name: String) {
            builder.remove(name)
        }
    }
}
