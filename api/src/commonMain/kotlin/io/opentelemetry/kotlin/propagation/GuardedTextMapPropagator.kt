package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.reportApiUserCodeError

/**
 * Wraps a [TextMapPropagator] so that an exception thrown by user-supplied code (a [TextMapGetter],
 * a [TextMapSetter], or a user-supplied propagator inside a composite) never escapes. Injection
 * stops at the failure, extraction returns the supplied context unchanged, and [fields] returns
 * an empty collection. Each caught exception is reported.
 */
@OptIn(ExperimentalApi::class)
internal class GuardedTextMapPropagator(
    private val delegate: TextMapPropagator,
) : TextMapPropagator {

    override fun fields(): Collection<String> = try {
        delegate.fields()
    } catch (e: Throwable) {
        reportApiUserCodeError(e, "TextMapPropagator.fields threw")
        emptyList()
    }

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        try {
            delegate.inject(context, carrier, setter)
        } catch (e: Throwable) {
            reportApiUserCodeError(e, "TextMapPropagator.inject threw")
        }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = try {
        delegate.extract(context, carrier, getter)
    } catch (e: Throwable) {
        reportApiUserCodeError(e, "TextMapPropagator.extract threw")
        context
    }
}

@OptIn(ExperimentalApi::class)
internal fun TextMapPropagator.guarded(): TextMapPropagator =
    this as? GuardedTextMapPropagator ?: GuardedTextMapPropagator(this)
