package io.opentelemetry.kotlin.error

/** Restore an interrupt swallowed by the process-wide SDK error handler. */
internal expect fun Throwable.restoreCaughtInterruptIfNeeded()
