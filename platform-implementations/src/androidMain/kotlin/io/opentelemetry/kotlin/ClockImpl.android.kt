package io.opentelemetry.kotlin

public actual fun getCurrentTimeNanos(): Long {
    // Android can accept millisecond resolution per issue #963
    // Following opentelemetry-android approach
    return System.currentTimeMillis() * 1_000_000L
}
