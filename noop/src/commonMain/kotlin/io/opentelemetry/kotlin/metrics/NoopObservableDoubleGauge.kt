package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableDoubleGauge].
 */
@ExperimentalApi
internal class NoopObservableDoubleGauge(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : ObservableDoubleGauge {
    override fun registerCallback(callback: (ObservableDoubleMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
