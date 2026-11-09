package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext

/**
 * Converts an opentelemetry-kotlin [Context] to an opentelemetry-java context, e.g. to pass it to
 * Java instrumentation.
 */
@ExperimentalApi
public fun Context.toOtelJavaContext(): OtelJavaContext {
    return (this as? ContextAdapter)?.impl ?: OtelJavaContextAdapter(this)
}
