package io.opentelemetry.kotlin

@OptIn(ExperimentalApi::class)
internal class ClockImpl : Clock {
    override fun now(): Long = getCurrentTimeNanos()
}
