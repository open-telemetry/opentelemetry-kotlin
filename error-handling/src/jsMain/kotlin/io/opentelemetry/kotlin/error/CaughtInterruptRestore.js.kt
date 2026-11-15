package io.opentelemetry.kotlin.error

internal actual fun Throwable.restoreCaughtInterruptIfNeeded() = Unit
