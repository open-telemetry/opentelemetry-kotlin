package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

/**
 * Wraps a [TextMapPropagator] so that an exception thrown by user-supplied code (a [TextMapGetter],
 * a [TextMapSetter], or a user-supplied propagator inside a composite) never escapes. Injection
 * stops at the failure, extraction returns the supplied context unchanged, and [fields] returns
 * an empty collection.
 */
@OptIn(ExperimentalApi::class)
internal class GuardedTextMapPropagator(
    private val delegate: TextMapPropagator,
) : TextMapPropagator {

    override fun fields(): Collection<String> = try {
        delegate.fields()
    } catch (ignored: Throwable) {
        emptyList()
    }

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        try {
            delegate.inject(context, carrier, setter)
        } catch (ignored: Throwable) {
        }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = try {
        delegate.extract(context, carrier, getter)
    } catch (ignored: Throwable) {
        context
    }
}

@OptIn(ExperimentalApi::class)
internal fun TextMapPropagator.guarded(): TextMapPropagator =
    this as? GuardedTextMapPropagator ?: GuardedTextMapPropagator(this)
