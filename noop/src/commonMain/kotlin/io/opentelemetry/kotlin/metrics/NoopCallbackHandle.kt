package io.opentelemetry.kotlin.metrics

internal object NoopCallbackHandle : AutoCloseable {
    override fun close() = Unit
}
