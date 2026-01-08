package io.opentelemetry.kotlin

/**
 * Gets the time in nanoseconds since the Java epoch (January 1, 1970, 00:00:00 UTC).
 *
 * If nanosecond precision is not possible to implement on a specific platform, it is acceptable
 * to return a value with millisecond precision in nanosecond units.
 */
public expect fun getCurrentTimeNanos(): Long
