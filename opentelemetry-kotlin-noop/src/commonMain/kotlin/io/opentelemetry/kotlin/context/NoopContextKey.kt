package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class NoopContextKey<T> : ContextKey<T> {
    override val name: String = ""
}
