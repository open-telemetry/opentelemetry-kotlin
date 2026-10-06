package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import java.util.WeakHashMap

internal class ContextKeyRepository {

    companion object {
        val INSTANCE = ContextKeyRepository()
    }

    // only hold keys not created by the compat layer
    private val foreignKeys = WeakHashMap<ContextKey<*>, OtelJavaContextKey<*>>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: ContextKey<T>): OtelJavaContextKey<T> {
        if (key is ContextKeyAdapter) {
            return key.impl
        }
        return synchronized(foreignKeys) {
            foreignKeys.getOrPut(key) {
                OtelJavaContextKey.named<T>(key.toString())
            }
        } as OtelJavaContextKey<T>
    }
}
