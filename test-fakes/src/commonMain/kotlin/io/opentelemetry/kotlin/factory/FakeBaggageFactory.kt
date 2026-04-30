package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageCreationAction
import io.opentelemetry.kotlin.baggage.FakeBaggage

class FakeBaggageFactory : BaggageFactory {

    override fun empty(): Baggage = FakeBaggage()

    override fun create(action: BaggageCreationAction.() -> Unit): Baggage {
        FakeBaggageCreationAction.action()
        return FakeBaggage()
    }

    private object FakeBaggageCreationAction : BaggageCreationAction {
        override fun put(name: String, value: String, metadata: String) = Unit
        override fun remove(name: String) = Unit
    }
}
