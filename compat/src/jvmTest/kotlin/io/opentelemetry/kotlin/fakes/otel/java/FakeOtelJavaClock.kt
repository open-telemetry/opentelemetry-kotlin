package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaClock

/**
 * Wall ([now]) and monotonic ([nanoTime]) readings move together via [advance], and can be set
 * apart to mimic the wall clock being stepped while the monotonic clock is not.
 */
internal class FakeOtelJavaClock(start: Long = 0) : OtelJavaClock {
    var nanoseconds = start
    var monotonicNanoseconds = start

    fun advance(nanos: Long) {
        nanoseconds += nanos
        monotonicNanoseconds += nanos
    }

    override fun now(): Long = nanoseconds
    override fun nanoTime(): Long = monotonicNanoseconds
}
