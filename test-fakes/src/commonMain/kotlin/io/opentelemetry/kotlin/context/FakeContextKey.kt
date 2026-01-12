package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeContextKey<T>(
    override val name: String = "key"
) : ContextKey<T>
