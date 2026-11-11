package io.opentelemetry.kotlin.error

internal actual fun Throwable.restoreCaughtInterruptIfNeeded() {
    if (this is InterruptedException) {
        Thread.currentThread().interrupt()
    }
}
