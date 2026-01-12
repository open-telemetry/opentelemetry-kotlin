package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class ContextKeyImpl<T>(override val name: String) : ContextKey<T>
