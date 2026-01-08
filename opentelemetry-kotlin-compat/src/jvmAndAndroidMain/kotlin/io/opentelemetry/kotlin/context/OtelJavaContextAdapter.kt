package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

@ExperimentalApi
internal class OtelJavaContextAdapter(
    private val impl: Context,
    private val repository: OtelJavaContextKeyRepository = OtelJavaContextKeyRepository.INSTANCE,
) : OtelJavaContext {

    override fun <V : Any?> get(key: OtelJavaContextKey<V>): V? {
        return impl.get(repository.get(key))
    }

    override fun <V : Any> with(key: OtelJavaContextKey<V>, value: V): OtelJavaContext {
        val ctx = impl.set(repository.get(key), value)
        return OtelJavaContextAdapter(ctx, repository)
    }
}
