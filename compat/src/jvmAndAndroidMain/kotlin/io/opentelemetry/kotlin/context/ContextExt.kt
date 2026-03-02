
package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContext

public fun Context.toOtelJavaContext(): OtelJavaContext {
    return (this as? ContextAdapter)?.impl ?: OtelJavaContextAdapter(this)
}
