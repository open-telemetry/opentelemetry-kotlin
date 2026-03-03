package io.opentelemetry.kotlin
internal class ClockImpl : Clock {
    override fun now(): Long = getCurrentTimeNanos()
}
