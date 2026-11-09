package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

/**
 * Returns the opentelemetry-java context key that this [ContextKey] maps to, so a value stored
 * from Kotlin can be read from a Java context.
 */
@ExperimentalApi
public fun <T> ContextKey<T>.toOtelJavaContextKey(): OtelJavaContextKey<T> =
    ContextKeyRepository.INSTANCE.get(this)
