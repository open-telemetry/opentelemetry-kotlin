package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongGauge
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault

@OptIn(ExperimentalApi::class)
@ThreadSafe
internal class ObservableLongGaugeAdapter(
    private val impl: OtelJavaMeter,
    override val name: String,
    override val unit: String?,
    override val description: String?,
    callbacks: Array<out (ObservableLongMeasurement) -> Unit>,
    private val sdkErrorHandler: SdkErrorHandler,
) : ObservableLongGauge {

    private val lock = Any()
    private val callbackHandles = mutableSetOf<OtelJavaObservableLongGauge>()
    private var closed = false

    override val measurement: ObservableLongMeasurement by lazy {
        sdkErrorHandler.sdkGuardOrDefault(null, "ObservableLongGauge.measurement failed") {
            val builder = impl.gaugeBuilder(name).ofLongs()
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
            ObservableLongMeasurementAdapter(builder.buildObserver(), sdkErrorHandler)
        } ?: NoopOpenTelemetry.meterProvider.getMeter("").createObservableLongGauge(name).measurement
    }

    init {
        callbacks.forEach { registerCallback(it) }
    }

    override fun registerCallback(callback: (ObservableLongMeasurement) -> Unit): AutoCloseable {
        synchronized(lock) {
            if (closed) {
                return NoopObservableCounterRegistration
            }
        }

        val javaGauge = sdkErrorHandler.sdkGuardOrDefault(null, "ObservableLongGauge.registerCallback failed") {
            val builder = impl.gaugeBuilder(name).ofLongs()
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
            builder.buildWithCallback { measurement ->
                sdkErrorHandler.guard("ObservableLongGauge callback failed") {
                    callback(ObservableLongMeasurementAdapter(measurement, sdkErrorHandler))
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
