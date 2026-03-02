package io.opentelemetry.kotlin.context
internal class ContextKeyImpl<T>(override val name: String) : ContextKey<T>
