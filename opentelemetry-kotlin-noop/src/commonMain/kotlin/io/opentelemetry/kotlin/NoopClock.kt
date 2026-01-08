package io.opentelemetry.kotlin

@OptIn(ExperimentalApi::class)
internal object NoopClock : Clock {
    override fun now(): Long = 0L
}
