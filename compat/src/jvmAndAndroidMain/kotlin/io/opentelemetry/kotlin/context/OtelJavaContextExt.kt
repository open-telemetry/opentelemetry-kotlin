package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext

/**
 * Converts an opentelemetry-java context to an opentelemetry-kotlin [Context], e.g. to use a
 * context received from Java instrumentation as a span parent.
 */
@ExperimentalApi
public fun OtelJavaContext.toOtelKotlinContext(): Context {
    return (this as? OtelJavaContextAdapter)?.impl ?: ContextAdapter(this)
}
