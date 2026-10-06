package io.opentelemetry.kotlin

/**
 * Exposes the [Clock] that produced an object's timestamps.
 */
public interface ClockProvider {
    public val clock: Clock
}
