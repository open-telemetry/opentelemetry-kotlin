package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter

internal class OtelJavaTextMapGetterAdapter(
    private val delegate: TextMapGetter<Any>,
) : OtelJavaTextMapGetter<Any> {

    override fun keys(carrier: Any): Iterable<String> = delegate.keys(carrier)

    override fun get(carrier: Any?, key: String): String? {
        if (carrier == null) {
            return null
        }
        return delegate.get(carrier, key)
    }
}
