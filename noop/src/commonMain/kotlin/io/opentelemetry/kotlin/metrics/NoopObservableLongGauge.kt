package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableLongGauge].
 */
@ExperimentalApi
internal class NoopObservableLongGauge(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : ObservableLongGauge {
    override fun registerCallback(callback: (ObservableLongMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
