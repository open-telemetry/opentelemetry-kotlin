package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableLongCounter].
 */
@OptIn(ExperimentalApi::class)
internal class NoopObservableLongCounter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : ObservableLongCounter {
    override val measurement: ObservableLongMeasurement = NoopObservableLongMeasurement()

    override fun registerCallback(callback: (ObservableLongMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
