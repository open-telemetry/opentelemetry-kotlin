package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextAdapter
import io.opentelemetry.kotlin.context.toOtelKotlinContext

@OptIn(ExperimentalApi::class)
internal class TextMapPropagatorAdapter(
    internal val impl: OtelJavaTextMapPropagator,
) : TextMapPropagator {

    override fun fields(): Collection<String> = impl.fields()

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        val javaCtx = context.toOtelJavaContext() ?: return
        val javaSetter = setter.toOtelJavaTextMapSetter() ?: return
        impl.inject(javaCtx, carrier, javaSetter)
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        val javaCtx = context.toOtelJavaContext() ?: return context
        val javaGetter = getter.toOtelJavaTextMapGetter() ?: return context
        return impl.extract(javaCtx, carrier, javaGetter).toOtelKotlinContext()
    }

    private fun Context.toOtelJavaContext(): OtelJavaContext? = (this as? ContextAdapter)?.impl

    @Suppress("UNCHECKED_CAST")
    private fun <T> TextMapSetter<T>.toOtelJavaTextMapSetter(): OtelJavaTextMapSetter<T>? {
        val delegate = this as? TextMapSetter<Any> ?: return null
        return OtelJavaTextMapSetterAdapter(delegate) as? OtelJavaTextMapSetter<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> TextMapGetter<T>.toOtelJavaTextMapGetter(): OtelJavaTextMapGetter<T>? {
        val delegate = this as? TextMapGetter<Any> ?: return null
        return OtelJavaTextMapGetterAdapter(delegate) as? OtelJavaTextMapGetter<T>
    }
}
