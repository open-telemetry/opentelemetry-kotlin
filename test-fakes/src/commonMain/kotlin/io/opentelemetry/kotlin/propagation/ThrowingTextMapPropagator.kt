package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

@OptIn(ExperimentalApi::class)
object ThrowingTextMapPropagator : TextMapPropagator {
    override fun fields(): Collection<String> = error("fields failed")
    override fun <T> inject(context: Context, carrier: T?, setter: TextMapSetter<T>): Unit = error("inject failed")
    override fun <T> extract(context: Context, carrier: T?, getter: TextMapGetter<T>): Context = error("extract failed")
}
