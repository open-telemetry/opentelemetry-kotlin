package io.opentelemetry.kotlin.context

internal data class NoopContextKey<T>(internal val name: String) : ContextKey<T> {
    override fun toString() = name
}
