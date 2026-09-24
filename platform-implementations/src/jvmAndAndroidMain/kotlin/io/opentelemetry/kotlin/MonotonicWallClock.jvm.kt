package io.opentelemetry.kotlin

/**
 * Wall-clock nanos anchored to [elapsedRealtimeNanos].
 *
 * The baseline is captured once. Later readings add the elapsed-realtime delta, so adjustments to
 * the wall clock (NTP, manual changes) cannot move time backwards or jump it forward.
 */
internal class MonotonicWallClock(
    wallTimeMillis: () -> Long,
    private val elapsedRealtimeNanos: () -> Long,
) {
    private val baselineNanos: Long

    init {
        val wallNanos = wallTimeMillis() * 1_000_000L
        val elapsedNanos = elapsedRealtimeNanos()
        baselineNanos = wallNanos - elapsedNanos
    }

    fun now(): Long {
        val elapsedNanos = elapsedRealtimeNanos()
        return baselineNanos + elapsedNanos
    }
}
