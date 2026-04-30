package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter

@OptIn(ExperimentalApi::class)
internal class OtelJavaTextMapSetterAdapter<C : Any>(
    private val delegate: TextMapSetter<C>,
) : OtelJavaTextMapSetter<C> {

    override fun set(carrier: C?, key: String, value: String) {
        if (carrier == null) {
            return
        }
        delegate.set(carrier, key, value)
    }
}
