package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableDoubleGauge].
 */
@OptIn(ExperimentalApi::class)
internal object NoopObservableDoubleGauge : ObservableDoubleGauge {
    override val name: String = ""
    override val unit: String? = null
    override val description: String? = null

    override val measurement: ObservableDoubleMeasurement = NoopObservableDoubleMeasurement()

    override fun registerCallback(callback: (ObservableDoubleMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
