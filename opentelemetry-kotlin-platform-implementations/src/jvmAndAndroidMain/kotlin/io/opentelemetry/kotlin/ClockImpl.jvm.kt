package io.opentelemetry.kotlin

public actual fun getCurrentTimeNanos(): Long {
    return System.currentTimeMillis() * 1_000_000L
}
