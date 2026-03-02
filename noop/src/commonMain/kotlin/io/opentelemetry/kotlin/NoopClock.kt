package io.opentelemetry.kotlin
internal object NoopClock : Clock {
    override fun now(): Long = 0L
}
