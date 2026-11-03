package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableLongCounter].
 */
@ExperimentalApi
internal class NoopObservableLongCounter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : ObservableLongCounter {
    override fun registerCallback(callback: (ObservableLongMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
