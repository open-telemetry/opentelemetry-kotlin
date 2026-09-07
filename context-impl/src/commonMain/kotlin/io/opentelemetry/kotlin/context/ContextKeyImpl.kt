package io.opentelemetry.kotlin.context

public class ContextKeyImpl<T>(internal val name: String) : ContextKey<T> {
    override fun toString() = name
}
