package io.opentelemetry.kotlin

/**
 * A latch for error reports from hot paths: a failure that repeats on every call is reported the
 * first time only, so it cannot flood the error handler.
 */
public class ReportOnce {

    private val reported = AtomicBoolean(false)

    /**
     * Returns true on the first call, when the caller should report, and false on later calls.
     */
    public fun tryAcquire(): Boolean = reported.compareAndSet(expect = false, update = true)
}
