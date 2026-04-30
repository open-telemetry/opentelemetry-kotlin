package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter

internal class OtelJavaTextMapSetterAdapter(
    private val delegate: TextMapSetter<Any>,
) : OtelJavaTextMapSetter<Any> {

    override fun set(carrier: Any?, key: String, value: String) {
        if (carrier != null) {
            delegate.set(carrier, key, value)
        }
    }
}
