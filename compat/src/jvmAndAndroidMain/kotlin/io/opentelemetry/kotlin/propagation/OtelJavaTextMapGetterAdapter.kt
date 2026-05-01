package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter

@OptIn(ExperimentalApi::class)
internal class OtelJavaTextMapGetterAdapter<C : Any>(
    private val delegate: TextMapGetter<C>,
) : OtelJavaTextMapGetter<C> {

    override fun keys(carrier: C): Iterable<String> = delegate.keys(carrier)

    override fun get(carrier: C?, key: String): String? {
        if (carrier == null) {
            return null
        }
        return delegate.get(carrier, key)
    }
}
