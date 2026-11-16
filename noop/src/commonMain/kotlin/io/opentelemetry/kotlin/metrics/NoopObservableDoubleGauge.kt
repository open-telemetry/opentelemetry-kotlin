package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableDoubleGauge].
 */
@OptIn(ExperimentalApi::class)
internal class NoopObservableDoubleGauge(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : ObservableDoubleGauge {
    override val measurement: ObservableDoubleMeasurement = NoopObservableDoubleMeasurement()

    override fun registerCallback(callback: (ObservableDoubleMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
