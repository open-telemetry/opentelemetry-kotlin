package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

@OptIn(ExperimentalApi::class)
class FakeTextMapPropagator(
    private val fields: List<String> = emptyList(),
    private val onExtract: (Context) -> Context = { it },
) : TextMapPropagator {

    var injectCalled: Boolean = false
        private set

    var extractCalled: Boolean = false
        private set

    override fun fields(): Collection<String> = fields

    override fun <T> inject(context: Context, carrier: T?, setter: TextMapSetter<T>) {
        injectCalled = true
        fields.forEach { field -> setter.set(carrier, field, "set-by-$field") }
    }

    override fun <T> extract(context: Context, carrier: T?, getter: TextMapGetter<T>): Context {
        extractCalled = true
        return onExtract(context)
    }
}
