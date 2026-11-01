package io.opentelemetry.kotlin.metrics

class FakeObservableDoubleCounter(
    override val name: String,
    override val unit: String? = null,
    override val description: String? = null,
    initialCallbacks: List<(ObservableDoubleMeasurement) -> Unit> = emptyList(),
) : ObservableDoubleCounter {
    private val callbackRegistry = FakeAsynchronousCallbackRegistry<ObservableDoubleMeasurement>()

    val callbacks: List<(ObservableDoubleMeasurement) -> Unit>
        get() = callbackRegistry.callbacks

    init {
        initialCallbacks.forEach(callbackRegistry::registerPermanently)
    }

    override fun registerCallback(callback: (ObservableDoubleMeasurement) -> Unit): AutoCloseable =
        callbackRegistry.register(callback)

    override fun close() {
        callbackRegistry.close()
    }

    fun invokeCallbacks(measurement: ObservableDoubleMeasurement) {
        callbackRegistry.invoke(measurement)
    }
}
