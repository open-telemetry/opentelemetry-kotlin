package io.opentelemetry.kotlin.propagation.utils

/**
 * Runs [action], which may call user-supplied code, swallowing anything it throws so that it
 * cannot destabilize the host.
 */
public inline fun safely(action: () -> Unit) {
    try {
        action()
    } catch (ignored: Throwable) {
        // swallow
    }
}

/**
 * As [safely], but for code that returns a value. Returns [default] if [action] throws.
 */
public inline fun <T> safely(default: T, action: () -> T): T = try {
    action()
} catch (ignored: Throwable) {
    default
}
