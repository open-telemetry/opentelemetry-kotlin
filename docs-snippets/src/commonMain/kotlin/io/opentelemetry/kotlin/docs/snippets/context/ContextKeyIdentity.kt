package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.context.rootContext

// region context-key-identity
// Do: declare each key once, as a shared constant.
private val FEATURE_ENABLED: ContextKey<Boolean> = createContextKey("feature.enabled")

fun contextKeyIdentity() {
    val ctx = rootContext().set(FEATURE_ENABLED, true)
    println(ctx.get(FEATURE_ENABLED)) // true

    // Don't: a second key with the same name is a different key.
    val lookalike: ContextKey<Boolean> = createContextKey("feature.enabled")
    println(ctx.get(lookalike)) // null
}
// end-region
