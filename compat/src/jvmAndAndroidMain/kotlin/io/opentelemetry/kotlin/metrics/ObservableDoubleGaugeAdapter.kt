package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleGauge
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault

@ExperimentalApi
@ThreadSafe
internal class ObservableDoubleGaugeAdapter(
    private val impl: OtelJavaMeter,
    override val name: String,
    override val unit: String?,
    override val description: String?,
    callbacks: Array<out (ObservableDoubleMeasurement) -> Unit>,
    private val sdkErrorHandler: SdkErrorHandler,
) : ObservableDoubleGauge {

    private val lock = Any()
    private val callbackHandles = mutableSetOf<OtelJavaObservableDoubleGauge>()
    private var closed = false

    override val measurement: ObservableDoubleMeasurement by lazy {
        sdkErrorHandler.sdkGuardOrDefault(null, "ObservableDoubleGauge.measurement failed") {
            val builder = impl.gaugeBuilder(name)
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
            ObservableDoubleMeasurementAdapter(builder.buildObserver(), sdkErrorHandler)
        } ?: NoopOpenTelemetry.meterProvider.getMeter("").createObservableDoubleGauge(name).measurement
    }

    init {
        callbacks.forEach { registerCallback(it) }
    }

    override fun registerCallback(callback: (ObservableDoubleMeasurement) -> Unit): AutoCloseable {
        synchronized(lock) {
            if (closed) {
                return NoopObservableCounterRegistration
            }
        }

        val javaGauge = sdkErrorHandler.sdkGuardOrDefault(null, "ObservableDoubleGauge.registerCallback failed") {
            val builder = impl.gaugeBuilder(name)
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
            builder.buildWithCallback { measurement ->
                sdkErrorHandler.guard("ObservableDoubleGauge callback failed") {
                    callback(ObservableDoubleMeasurementAdapter(measurement, sdkErrorHandler))
                }
            }
        } ?: return NoopObservableCounterRegistration

        synchronized(lock) {
            if (closed) {
                javaGauge.close()
                return NoopObservableCounterRegistration
            }
            callbackHandles.add(javaGauge)
        }

        return CallbackRegistrationHandle {
            synchronized(lock) {
                if (callbackHandles.remove(javaGauge)) {
                    javaGauge.close()
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
