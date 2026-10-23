package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates a new [ContextKey] with the given name. The name is used for debugging and does NOT
 * uniquely identify values - use the [ContextKey] itself for that.
 *
 * [T] represents the type of the value that is stored in the context.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/context/#create-a-key
 */
@ExperimentalApi
public fun <T> createContextKey(name: String): ContextKey<T> = NamedContextKey(name)

@OptIn(ExperimentalApi::class)
private class NamedContextKey<T>(private val name: String) : ContextKey<T> {
    override fun toString() = name
}
