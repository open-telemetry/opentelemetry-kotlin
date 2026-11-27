package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableLongGauge].
 */
@OptIn(ExperimentalApi::class)
internal object NoopObservableLongGauge : ObservableLongGauge {
    override val name: String = ""
    override val unit: String? = null
    override val description: String? = null

    override val measurement: ObservableLongMeasurement = NoopObservableLongMeasurement()

    override fun registerCallback(callback: (ObservableLongMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
