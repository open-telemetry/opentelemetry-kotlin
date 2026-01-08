package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

@OptIn(ExperimentalApi::class)
public fun <T> ContextKey<T>.toOtelJavaContextKey(): OtelJavaContextKey<T> = (this as ContextKeyAdapter).impl
