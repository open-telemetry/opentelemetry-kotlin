package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

/**
 * Wraps a [B3Propagator] so that an exception thrown by a user-supplied [TextMapGetter] or
 * [TextMapSetter] never escapes. Injection stops at the failure and extraction returns the supplied
 * context unchanged. Each caught exception is reported.
 */
@OptIn(ExperimentalApi::class)
internal class GuardedB3Propagator(
    private val delegate: B3Propagator,
) : TextMapPropagator {

    override fun fields(): Collection<String> = delegate.fields()

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        try {
            delegate.inject(context, carrier, setter)
        } catch (e: Throwable) {
            report(e, "TextMapPropagator.inject threw")
        }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = try {
        delegate.extract(context, carrier, getter)
    } catch (e: Throwable) {
        report(e, "TextMapPropagator.extract threw")
        context
    }

    private fun report(exc: Throwable, message: String) {
        reportError(SdkError.UserCodeError(exc, message, SdkErrorSeverity.WARNING))
    }
}
