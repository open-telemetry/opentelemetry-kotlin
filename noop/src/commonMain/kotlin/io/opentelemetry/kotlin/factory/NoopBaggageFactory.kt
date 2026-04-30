package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageCreationAction
import io.opentelemetry.kotlin.baggage.NoopBaggage

@OptIn(ExperimentalApi::class)
internal object NoopBaggageFactory : BaggageFactory {

    override fun empty(): Baggage = NoopBaggage

    override fun create(action: BaggageCreationAction.() -> Unit): Baggage = NoopBaggage
}
