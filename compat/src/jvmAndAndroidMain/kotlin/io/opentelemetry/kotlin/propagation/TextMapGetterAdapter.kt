package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter

internal class TextMapGetterAdapter<C : Any>(
    private val delegate: OtelJavaTextMapGetter<C>,
) : TextMapGetter<C> {

    override fun keys(carrier: C): Collection<String> = delegate.keys(carrier).toList()

    override fun get(carrier: C, key: String): String? = delegate.get(carrier, key)
}
