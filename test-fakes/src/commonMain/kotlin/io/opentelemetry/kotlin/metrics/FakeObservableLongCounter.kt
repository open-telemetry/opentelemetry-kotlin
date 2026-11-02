package io.opentelemetry.kotlin.metrics

class FakeObservableLongCounter(
    override val name: String,
    override val unit: String? = null,
    override val description: String? = null,
    initialCallbacks: List<(ObservableLongMeasurement) -> Unit> = emptyList(),
) : ObservableLongCounter {
    private val callbackRegistry = FakeAsynchronousCallbackRegistry<ObservableLongMeasurement>()

    val callbacks: List<(ObservableLongMeasurement) -> Unit>
        get() = callbackRegistry.callbacks

    init {
        initialCallbacks.forEach(callbackRegistry::registerPermanently)
    }

    override fun registerCallback(callback: (ObservableLongMeasurement) -> Unit): AutoCloseable =
        callbackRegistry.register(callback)

    override fun close() {
        callbackRegistry.close()
    }

    fun invokeCallbacks(measurement: ObservableLongMeasurement) {
        callbackRegistry.invoke(measurement)
    }
}
