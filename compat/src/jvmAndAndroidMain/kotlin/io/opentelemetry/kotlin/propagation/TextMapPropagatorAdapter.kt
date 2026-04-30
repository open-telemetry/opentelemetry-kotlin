package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.context.toOtelKotlinContext

@OptIn(ExperimentalApi::class)
internal class TextMapPropagatorAdapter(
    internal val impl: OtelJavaTextMapPropagator,
) : TextMapPropagator {

    override fun fields(): Collection<String> = impl.fields()

    @Suppress("UNCHECKED_CAST")
    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        val javaSetter =
            OtelJavaTextMapSetterAdapter(setter as TextMapSetter<Any>) as OtelJavaTextMapSetter<T>
        impl.inject(context.toOtelJavaContext(), carrier, javaSetter)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        val javaGetter =
            OtelJavaTextMapGetterAdapter(getter as TextMapGetter<Any>) as OtelJavaTextMapGetter<T>
        val result = impl.extract(context.toOtelJavaContext(), carrier, javaGetter)
        return result.toOtelKotlinContext()
    }
}
