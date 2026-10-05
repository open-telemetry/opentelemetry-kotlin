package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.aliases.OtelJavaClock
import io.opentelemetry.kotlin.clock.ClockAdapter

/**
 * Returns the clock the Java SDK should time telemetry with. A [ClockAdapter] hands back the Java clock
 * it decorates.
 */
internal fun Clock.toOtelJavaClock(): OtelJavaClock = when (this) {
    is ClockAdapter -> impl
    else -> OtelJavaClockWrapper(this)
}

/**
 * Adapts a user-supplied [Clock]. [Clock] has no monotonic reading, so [nanoTime] falls back to [now].
 */
internal class OtelJavaClockWrapper(
    private val impl: Clock
) : OtelJavaClock {

    override fun now(): Long = impl.now()

    override fun nanoTime(): Long = impl.now()
}
