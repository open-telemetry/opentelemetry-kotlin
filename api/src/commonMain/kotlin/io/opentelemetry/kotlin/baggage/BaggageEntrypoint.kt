package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates a [Baggage] by configuring entries inside the [action] DSL block. Omitting [action]
 * returns the empty [Baggage].
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance: the Baggage API is
 * fully functional in the absence of an installed SDK.
 *
 * https://opentelemetry.io/docs/specs/otel/baggage/api/
 */
@ExperimentalApi
public fun createBaggage(action: BaggageCreationAction.() -> Unit = {}): Baggage {
    val builder = BaggageCreationActionImpl()
    builder.action()
    return builder.build()
}

@OptIn(ExperimentalApi::class)
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
