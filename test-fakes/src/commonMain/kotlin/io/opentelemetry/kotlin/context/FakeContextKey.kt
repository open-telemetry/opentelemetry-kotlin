package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
public data class FakeContextKey<T>(public val name: String = "key") : ContextKey<T> {
    override fun toString() = name
}
