package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.guardOrDefault

/**
 * Wraps a [TextMapPropagator] so that an exception thrown by user-supplied code (the propagator
 * itself, a [TextMapGetter] or a [TextMapSetter]) never escapes. Injection stops at the failure,
 * extraction returns the supplied context unchanged, and [fields] returns an empty collection.
 * Each caught exception is reported to [sdkErrorHandler].
 */
@OptIn(ExperimentalApi::class)
public class SdkGuardedTextMapPropagator(
    public val delegate: TextMapPropagator,
    private val sdkErrorHandler: SdkErrorHandler,
) : TextMapPropagator {

    override fun fields(): Collection<String> =
        sdkErrorHandler.guardOrDefault(emptyList(), "TextMapPropagator.fields failed") {
            delegate.fields()
        }

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        sdkErrorHandler.guard("TextMapPropagator.inject failed") {
            delegate.inject(context, carrier, setter)
        }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context =
        sdkErrorHandler.guardOrDefault(context, "TextMapPropagator.extract failed") {
            delegate.extract(context, carrier, getter)
        }
}

/**
 * Returns this propagator wrapped in a [SdkGuardedTextMapPropagator], unless it is the noop
 * propagator or is already wrapped.
 */
@OptIn(ExperimentalApi::class)
public fun TextMapPropagator.guarded(sdkErrorHandler: SdkErrorHandler): TextMapPropagator = when {
    this === createNoopPropagator() || this is SdkGuardedTextMapPropagator -> this
    else -> SdkGuardedTextMapPropagator(this, sdkErrorHandler)
}
