package io.opentelemetry.kotlin.error

/** Preserve the JVM thread's interrupt status when a guard swallows an exception. */
internal expect fun Throwable.restoreInterruptIfNeeded()
