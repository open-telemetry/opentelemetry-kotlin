package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import java.util.Collections
import java.util.WeakHashMap

internal class ContextKeyRepository {

    companion object {
        val INSTANCE = ContextKeyRepository()
    }

    private val impl =
        Collections.synchronizedMap(WeakHashMap<ContextKey<*>, OtelJavaContextKey<*>>())

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: ContextKey<T>): OtelJavaContextKey<T> {
        impl[key]?.let { return it as OtelJavaContextKey<T> }
        val candidate = if (key is ContextKeyAdapter) {
            key.impl
        } else {
            OtelJavaContextKey.named(key.toString())
        }
        return synchronized(impl) {
            impl[key] ?: candidate.also { impl[key] = it }
        } as OtelJavaContextKey<T>
    }
}
