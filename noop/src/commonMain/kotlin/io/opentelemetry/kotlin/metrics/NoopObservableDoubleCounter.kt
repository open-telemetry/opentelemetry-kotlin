package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [ObservableDoubleCounter].
 */
@ExperimentalApi
internal class NoopObservableDoubleCounter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : ObservableDoubleCounter {
    override fun registerCallback(callback: (ObservableDoubleMeasurement) -> Unit): AutoCloseable =
        NoopCallbackHandle

    override fun close() = Unit
}
