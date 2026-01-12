package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

@ExperimentalApi
internal class ContextKeyAdapter<T>(
    internal val impl: OtelJavaContextKey<T>
) : ContextKey<T> {
    override val name: String = impl.toString()
}
