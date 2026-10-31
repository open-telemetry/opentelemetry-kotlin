package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableLongUpDownCounter].
 */
@ExperimentalApi
internal class NoopObservableLongUpDownCounter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : ObservableLongUpDownCounter {
    override fun registerCallback(callback: (ObservableLongMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
