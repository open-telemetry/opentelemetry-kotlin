package io.opentelemetry.kotlin.context

internal class ContextKeyImpl<T>(internal val name: String) : ContextKey<T> {
    override fun toString() = name
}
