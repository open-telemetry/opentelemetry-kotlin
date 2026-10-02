package io.opentelemetry.kotlin.clock

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.aliases.OtelJavaClock

internal class ClockAdapter(
    internal val impl: OtelJavaClock = OtelJavaClock.getDefault()
) : Clock {
    override fun now(): Long = impl.now()
}
