package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContext

public fun OtelJavaContext.toOtelKotlinContext(): Context {
    return ContextAdapter(this)
}
