package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntryMetadata

/**
 * Copies opentelemetry-kotlin [Baggage] into opentelemetry-java baggage, keeping each entry's
 * value and metadata.
 */
@ExperimentalApi
public fun Baggage.toOtelJavaBaggage(): OtelJavaBaggage {
    val builder = OtelJavaBaggage.builder()
    asMap().forEach { (name, entry) ->
        builder.put(name, entry.value, OtelJavaBaggageEntryMetadata.create(entry.metadata.value))
    }
    return builder.build()
}

/**
 * Copies opentelemetry-java baggage into opentelemetry-kotlin [Baggage], keeping each entry's
 * value and metadata.
 */
@ExperimentalApi
public fun OtelJavaBaggage.toOtelKotlinApi(): Baggage = toOtelKotlinBaggage()

/**
 * Copies opentelemetry-java baggage into the Kotlin [Baggage] implementation.
 *
 * opentelemetry-java does not validate entries, so any entry that the Kotlin implementation
 * rejects (e.g. a key that is not a valid RFC 7230 token) is dropped.
 */
@OptIn(ExperimentalApi::class)
internal fun OtelJavaBaggage.toOtelKotlinBaggage(): Baggage = createBaggage {
    forEach { name, entry ->
        put(name, entry.value, entry.metadata.value)
    }
}
