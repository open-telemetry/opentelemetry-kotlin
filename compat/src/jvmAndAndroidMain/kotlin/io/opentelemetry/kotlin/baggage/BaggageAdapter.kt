package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntryMetadata

@OptIn(ExperimentalApi::class)
internal class BaggageAdapter(internal val impl: OtelJavaBaggage) : Baggage {

    override fun getValue(name: String): String? = impl.getEntryValue(name)

    override fun asMap(): Map<String, BaggageEntry> = impl.asMap().mapValues { (_, v) ->
        BaggageEntryAdapter(v)
    }

    override fun set(name: String, value: String): Baggage =
        BaggageAdapter(impl.toBuilder().put(name, value).build())

    override fun set(name: String, value: String, metadata: BaggageEntryMetadata): Baggage =
        BaggageAdapter(
            impl.toBuilder().put(name, value, OtelJavaBaggageEntryMetadata.create(metadata.value)).build()
        )

    override fun remove(name: String): Baggage =
        if (impl.getEntryValue(name) == null) {
            this
        } else {
            BaggageAdapter(impl.toBuilder().remove(name).build())
        }
}
