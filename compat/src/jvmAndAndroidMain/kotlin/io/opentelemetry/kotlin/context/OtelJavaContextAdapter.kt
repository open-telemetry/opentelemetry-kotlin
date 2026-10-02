package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

/**
 * Java keys are wrapped in a fresh [ContextKeyAdapter] on each access. Adapters compare equality
 * by the Java key they wrap, so values are retrievable without a global key mapping.
 */
@ExperimentalApi
internal class OtelJavaContextAdapter(
    internal val impl: Context,
) : OtelJavaContext {

    override fun <V : Any?> get(key: OtelJavaContextKey<V>): V? {
        return impl.get(ContextKeyAdapter(key))
    }

    override fun <V : Any> with(key: OtelJavaContextKey<V>, value: V?): OtelJavaContext {
        val ctx = impl.set(ContextKeyAdapter(key), value)
        return OtelJavaContextAdapter(ctx)
    }
}
