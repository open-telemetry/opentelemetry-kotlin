package io.opentelemetry.kotlin.tracing

/**
 * Internal implementation of [TracerConfig].
 */
internal class TracerConfigImpl(override val enabled: Boolean) : TracerConfig
