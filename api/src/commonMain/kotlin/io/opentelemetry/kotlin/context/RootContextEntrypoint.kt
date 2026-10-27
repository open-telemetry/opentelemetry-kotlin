package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
private val ROOT_CONTEXT: Context = ContextValue()

/**
 * Returns the root [Context], which holds no values. The same instance is returned on every call.
 *
 * Calling [Context.attach] on the root context, or on any context derived from it, is a no-op
 * until a CurrentContextStorage is installed.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/context/
 */
@ExperimentalApi
internal fun rootContext(): Context = ROOT_CONTEXT
