package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

public fun <T> ContextKey<T>.toOtelJavaContextKey(): OtelJavaContextKey<T> =
    ContextKeyRepository.INSTANCE.get(this)
