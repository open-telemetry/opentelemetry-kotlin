package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableDoubleUpDownCounter].
 */
@ExperimentalApi
internal class NoopObservableDoubleUpDownCounter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : ObservableDoubleUpDownCounter {
    override fun registerCallback(callback: (ObservableDoubleMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
