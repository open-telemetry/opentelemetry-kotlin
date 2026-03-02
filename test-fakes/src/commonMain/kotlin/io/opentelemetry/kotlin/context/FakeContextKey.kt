package io.opentelemetry.kotlin.context
class FakeContextKey<T>(
    override val name: String = "key"
) : ContextKey<T>
