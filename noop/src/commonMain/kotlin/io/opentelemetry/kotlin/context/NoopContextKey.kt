package io.opentelemetry.kotlin.context
internal class NoopContextKey<T> : ContextKey<T> {
    override val name: String = ""
}
