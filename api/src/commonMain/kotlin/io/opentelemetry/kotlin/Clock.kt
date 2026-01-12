package io.opentelemetry.kotlin

/**
 * A clock that provides the current time in nanoseconds.
 */
@ExperimentalApi
public fun interface Clock {

    /**
     * Returns the current time in nanoseconds.
     */
    public fun now(): Long
}
