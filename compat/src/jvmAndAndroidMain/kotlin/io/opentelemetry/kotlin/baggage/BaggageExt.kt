package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntryMetadata

@OptIn(ExperimentalApi::class)
public fun Baggage.toOtelJavaBaggage(): OtelJavaBaggage {
    (this as? BaggageAdapter)?.let { return it.impl }

    val builder = OtelJavaBaggage.builder()
    asMap().forEach { (name, entry) ->
        builder.put(name, entry.value, OtelJavaBaggageEntryMetadata.create(entry.metadata.value))
    }
    return builder.build()
}
