package io.opentelemetry.kotlin.metrics

internal class CallbackRegistrationHandle(
    private val unregister: () -> Unit,
) : AutoCloseable {
    private var closed = false

    override fun close() {
        synchronized(this) {
            if (closed) {
                return
            }
            closed = true
            unregister()
        }
    }
}

internal object NoopObservableCounterRegistration : AutoCloseable {
    override fun close() = Unit
}
