package io.opentelemetry.kotlin.metrics

class FakeBatchCallback internal constructor(
    val measurements: List<ObservableMeasurement>,
    private val callback: () -> Unit,
    private val onClose: (FakeBatchCallback) -> Unit,
) : AutoCloseable {

    private var isClosed: Boolean = false

    fun collect() {
        if (!isClosed) {
            callback()
        }
    }

    override fun close() {
        if (!isClosed) {
            isClosed = true
            onClose(this)
        }
    }
}
