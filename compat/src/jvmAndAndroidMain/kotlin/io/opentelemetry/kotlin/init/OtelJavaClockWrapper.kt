package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.aliases.OtelJavaClock

internal class OtelJavaClockWrapper(
    private val impl: Clock
) : OtelJavaClock {

    override fun now(): Long = impl.now()

    override fun nanoTime(): Long = impl.now()
}
