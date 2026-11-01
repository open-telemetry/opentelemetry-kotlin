package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

@OptIn(ExperimentalApi::class)
internal class CompositeTextMapPropagator(
    private val delegates: List<TextMapPropagator>,
) : TextMapPropagator {

    private val fields: List<String> = delegates.flatMap(TextMapPropagator::fields).distinct()

    override fun fields(): Collection<String> = fields

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        delegates.forEach { delegate ->
            delegate.inject(context, carrier, setter)
        }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        var result = context
        delegates.forEach { delegate ->
            result = delegate.extract(result, carrier, getter)
        }
        return result
    }
}
