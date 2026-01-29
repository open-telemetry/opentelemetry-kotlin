package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * This function returns a new immutable [io.opentelemetry.kotlin.context.Context] that contains the key-value pairs
 * in the supplied map.
 */
@ThreadSafe
@ExperimentalApi
public fun Context.with(values: Map<String, Any>): Context {
    var ctx = this
    values.forEach { (key, value) ->
        ctx = ctx.set(createKey(key), value)
    }
    return ctx
}

/**
 * Sets this context as the implicit context for the duration of [block], automatically
 * detaching after [block] completes.
 */
@ExperimentalApi
@ThreadSafe
public inline fun <T> Context.asImplicitContext(block: () -> T): T {
    val scope = attach()
    try {
        return block()
    } finally {
        scope.detach()
    }
}
