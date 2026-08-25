package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

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
