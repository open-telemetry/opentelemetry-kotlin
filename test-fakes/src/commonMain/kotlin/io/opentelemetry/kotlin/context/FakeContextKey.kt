package io.opentelemetry.kotlin.context

data class FakeContextKey<T>(val name: String = "key") : ContextKey<T> {
    override fun toString() = name
}
