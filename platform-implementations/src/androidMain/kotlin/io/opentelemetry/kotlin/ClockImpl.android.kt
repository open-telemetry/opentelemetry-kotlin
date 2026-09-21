package io.opentelemetry.kotlin

import android.os.SystemClock

/**
 * An implementation of [Clock] that takes a baseline by subtracting
 * [SystemClock.elapsedRealtimeNanos] from [System.currentTimeMillis] and then
 * adds that baseline to [SystemClock.elapsedRealtimeNanos] to provide a monotonic wall-clock time.
 *
 * This avoids the downside of [System.currentTimeMillis] not being monotonic on Android (i.e.
 * the clock doesn't consistently tick when the process is in the background or cached). It also
 * avoids the problem of [SystemClock.elapsedRealtimeNanos] not providing wall-clock time.
 */
public actual fun getCurrentTimeNanos(): Long = AndroidClock.now()

private object AndroidClock {
    private val baselineNanos =
        System.currentTimeMillis() * 1_000_000L - SystemClock.elapsedRealtimeNanos()

    fun now(): Long = baselineNanos + SystemClock.elapsedRealtimeNanos()
}