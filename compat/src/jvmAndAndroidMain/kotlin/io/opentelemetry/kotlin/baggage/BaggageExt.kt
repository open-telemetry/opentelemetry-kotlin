package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntryMetadata

@OptIn(ExperimentalApi::class)
public fun Baggage.toOtelJavaBaggage(): OtelJavaBaggage = when (this) {
    is BaggageAdapter -> impl
    else -> {
        val builder = OtelJavaBaggage.builder()
        asMap().forEach { (name, entry) ->
            builder.put(name, entry.value, OtelJavaBaggageEntryMetadata.create(entry.metadata.value))
        }
        builder.build()
    }
}
