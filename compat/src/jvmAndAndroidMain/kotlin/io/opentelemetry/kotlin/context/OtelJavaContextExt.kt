package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext

@OptIn(ExperimentalApi::class)
public fun OtelJavaContext.toOtelKotlinContext(): Context {
    return ContextAdapter(this)
}
