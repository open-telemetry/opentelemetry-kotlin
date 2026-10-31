package io.opentelemetry.kotlin.metrics

internal class FakeAsynchronousCallbackRegistry<M : ObservableMeasurement> {
    private class Registration<M>(val callback: (M) -> Unit)

    private val registrations = mutableListOf<Registration<M>>()
    private var closed = false

    val callbacks: List<(M) -> Unit>
        get() = registrations.map { it.callback }

    fun register(callback: (M) -> Unit): AutoCloseable {
        if (closed) {
            return NoopHandle
        }

        val registration = Registration(callback)
        registrations.add(registration)
        return object : AutoCloseable {
            private var unregistered = false

            override fun close() {
                if (unregistered) {
                    return
                }
                unregistered = true
                registrations.remove(registration)
            }
        }
    }

    fun registerPermanently(callback: (M) -> Unit) {
        if (!closed) {
            registrations.add(Registration(callback))
        }
    }

    fun invoke(measurement: M) {
        if (!closed) {
            registrations.toList().forEach { it.callback(measurement) }
        }
    }

    fun close() {
        closed = true
        registrations.clear()
    }

    private object NoopHandle : AutoCloseable {
        override fun close() = Unit
    }
}
