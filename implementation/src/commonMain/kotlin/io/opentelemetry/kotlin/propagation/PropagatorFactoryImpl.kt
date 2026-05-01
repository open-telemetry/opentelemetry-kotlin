package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

@OptIn(ExperimentalApi::class)
internal class PropagatorFactoryImpl : PropagatorFactory {

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        return composite(propagators.toList())
    }

    override fun composite(propagators: List<TextMapPropagator>): TextMapPropagator {
        return when (propagators.size) {
            0 -> EmptyTextMapPropagator
            1 -> propagators.single()
            else -> CompositeTextMapPropagator(propagators.toList())
        }
    }

    override fun w3cBaggage(): TextMapPropagator = W3CBaggagePropagator

    private object EmptyTextMapPropagator : TextMapPropagator {
        override fun fields(): Collection<String> = emptyList()
        override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {}
        override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = context
    }
}
