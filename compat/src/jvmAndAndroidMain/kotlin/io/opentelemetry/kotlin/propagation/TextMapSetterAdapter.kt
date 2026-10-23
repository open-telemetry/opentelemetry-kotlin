package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter

internal class TextMapSetterAdapter<C : Any>(
    private val delegate: OtelJavaTextMapSetter<C>,
) : TextMapSetter<C> {

    override fun set(carrier: C, key: String, value: String) {
        delegate.set(carrier, key, value)
    }
}
