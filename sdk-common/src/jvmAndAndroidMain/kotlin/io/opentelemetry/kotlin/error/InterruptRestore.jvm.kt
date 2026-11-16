package io.opentelemetry.kotlin.error

internal actual fun Throwable.restoreInterruptIfNeeded() {
    if (this is InterruptedException) {
        Thread.currentThread().interrupt()
    }
}
