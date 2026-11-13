package io.opentelemetry.kotlin

public actual fun safeExceptionAttributes(throwable: Throwable): SafeExceptionAttributes =
    safeExceptionAttributesFallback(throwable)
