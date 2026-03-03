package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class ContextKeyImpl<T>(internal val name: String) : ContextKey<T> {
    override fun toString() = name
}
