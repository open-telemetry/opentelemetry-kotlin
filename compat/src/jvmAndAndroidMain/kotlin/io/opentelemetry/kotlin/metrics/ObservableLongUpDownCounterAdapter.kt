package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongUpDownCounter
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault

@ExperimentalApi
@ThreadSafe
internal class ObservableLongUpDownCounterAdapter(
    private val impl: OtelJavaMeter,
    override val name: String,
    override val unit: String?,
    override val description: String?,
    callbacks: Array<out (ObservableLongMeasurement) -> Unit>,
    private val sdkErrorHandler: SdkErrorHandler,
) : ObservableLongUpDownCounter {

    private val lock = Any()
    private val callbackHandles = mutableSetOf<OtelJavaObservableLongUpDownCounter>()
    private var closed = false

    init {
        callbacks.forEach { registerCallback(it) }
    }

    override fun registerCallback(callback: (ObservableLongMeasurement) -> Unit): AutoCloseable {
        synchronized(lock) {
            if (closed) {
                return NoopObservableCounterRegistration
            }
        }

        val javaCounter = sdkErrorHandler.sdkGuardOrDefault(
            null,
            "ObservableLongUpDownCounter.registerCallback failed",
        ) {
            val builder = impl.upDownCounterBuilder(name)
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
            builder.buildWithCallback { measurement ->
                sdkErrorHandler.guard("ObservableLongUpDownCounter callback failed") {
                    callback(ObservableLongMeasurementAdapter(measurement, sdkErrorHandler))
                }
            }
        } ?: return NoopObservableCounterRegistration

        synchronized(lock) {
            if (closed) {
                javaCounter.close()
                return NoopObservableCounterRegistration
            }
            callbackHandles.add(javaCounter)
        }

        return CallbackRegistrationHandle {
            synchronized(lock) {
                if (callbackHandles.remove(javaCounter)) {
                    javaCounter.close()
                }
            }
        }
    }

    override fun close() {
        synchronized(lock) {
            if (closed) {
                return
            }
            closed = true
            callbackHandles.toList().forEach { it.close() }
            callbackHandles.clear()
        }
    }
}
