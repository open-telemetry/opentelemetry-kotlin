package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.propagation.PropagatorFactory
import io.opentelemetry.kotlin.propagation.TextMapGetter
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapSetter

@OptIn(ExperimentalApi::class)
class FakePropagatorFactory : PropagatorFactory {

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator =
        FakeTextMapPropagator

    override fun composite(propagators: List<TextMapPropagator>): TextMapPropagator =
        FakeTextMapPropagator

    override fun w3cBaggage(): TextMapPropagator = FakeTextMapPropagator

    private object FakeTextMapPropagator : TextMapPropagator {
        override fun fields(): Collection<String> = emptyList()
        override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {}
        override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = context
    }
}
