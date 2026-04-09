package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

@ExperimentalApi
internal object NoopTextMapPropagator : TextMapPropagator {
    override fun fields(): Collection<String> = emptyList()
    override fun <C> inject(context: Context, carrier: C, setter: TextMapSetter<C>) {}
    override fun <C> extract(context: Context, carrier: C, getter: TextMapGetter<C>): Context = context
}
