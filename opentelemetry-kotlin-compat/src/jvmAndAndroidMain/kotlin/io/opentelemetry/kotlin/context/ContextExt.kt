@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext

@OptIn(ExperimentalApi::class)
public fun Context.toOtelJavaContext(): OtelJavaContext {
    return (this as? ContextAdapter)?.impl ?: OtelJavaContextAdapter(this)
}
