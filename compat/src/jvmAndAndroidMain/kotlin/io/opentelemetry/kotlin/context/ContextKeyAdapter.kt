package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

@ExperimentalApi
@JvmInline
internal value class ContextKeyAdapter<T>(
    internal val impl: OtelJavaContextKey<T>
) : ContextKey<T> {
    internal val name: String get() = impl.toString()
    override fun toString() = name
}
