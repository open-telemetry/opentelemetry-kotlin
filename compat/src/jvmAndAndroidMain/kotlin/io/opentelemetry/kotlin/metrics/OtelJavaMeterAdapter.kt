package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider

/**
 * Adapts a Kotlin [Meter] to a Java [OtelJavaMeter].
 *
 * TODO Until instruments are implemented on the Kotlin side, all instrument-builder calls delegate
 *  to Java OTel's no-op meter — i.e., builders, instruments, and observables are valid types
 *  but record nothing.
 */
internal class OtelJavaMeterAdapter(
    @Suppress("UnusedPrivateProperty") private val impl: Meter,
) : OtelJavaMeter by OtelJavaMeterProvider.noop().get("")
