package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

@ExperimentalApi
internal object NoopTextMapPropagator : TextMapPropagator {
    override fun fields(): Collection<String> = emptyList()
    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {}
    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = context
}
