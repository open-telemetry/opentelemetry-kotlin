package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaBatchCallback
import io.opentelemetry.kotlin.aliases.OtelJavaLongCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableMeasurement
import io.opentelemetry.kotlin.error.PlatformSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode

internal class OtelJavaMeterAdapter(
    private val impl: Meter,
    private val javaMeterProvider: OtelJavaMeterProvider = OtelJavaMeterProvider.noop(),
    private val sdkErrorHandler: SdkErrorHandler = PlatformSdkErrorHandler,
) : OtelJavaMeter by javaMeterProvider.get("") {
    override fun gaugeBuilder(name: String): io.opentelemetry.kotlin.aliases.OtelJavaDoubleGaugeBuilder =
        OtelJavaDoubleGaugeBuilderAdapter(impl, name, this)

    override fun counterBuilder(name: String): OtelJavaLongCounterBuilder =
        OtelJavaLongCounterBuilderAdapter(impl, name, javaMeterProvider)

    override fun upDownCounterBuilder(name: String): OtelJavaLongUpDownCounterBuilder =
        OtelJavaLongUpDownCounterBuilderAdapter(impl, name, javaMeterProvider)

    override fun batchCallback(
        callback: Runnable,
        observableMeasurement: OtelJavaObservableMeasurement,
        vararg additionalMeasurements: OtelJavaObservableMeasurement,
    ): OtelJavaBatchCallback {
        val measurements = (listOf(observableMeasurement) + additionalMeasurements).mapNotNull { candidate ->
            val converted = candidate.toKotlinMeasurementOrNull()
            if (converted == null) {
                sdkErrorHandler.reportError(
                    SdkError.ApiMisuse(
                        api = "Meter.batchCallback",
                        message = "Observable measurement is not backed by OpenTelemetry Kotlin; ignoring it.",
                        severity = SdkErrorSeverity.WARNING,
                    )
                )
            }
            converted
        }
        if (measurements.isEmpty()) {
            return noopJavaBatchCallback
        }

        return sdkErrorHandler.sdkGuardOrDefault(noopJavaBatchCallback, "Meter.batchCallback failed") {
            val registration = impl.batchCallback(
                measurements.first(),
                *measurements.drop(1).toTypedArray(),
            ) {
                sdkErrorHandler.sdkGuardOrDefault(Unit, "Meter.batchCallback callback failed") {
                    userCode { callback.run() }
                }
            }
            object : OtelJavaBatchCallback {
                override fun close() {
                    sdkErrorHandler.sdkGuardOrDefault(Unit, "Meter.batchCallback.close failed") {
                        registration.close()
                    }
                }
            }
        }
    }
}

private fun OtelJavaObservableMeasurement.toKotlinMeasurementOrNull(): ObservableMeasurement? = when (this) {
    is OtelJavaObservableLongMeasurementAdapter -> impl
    is OtelJavaObservableDoubleMeasurementAdapter -> impl
    else -> null
}

private val noopJavaBatchCallback: OtelJavaBatchCallback = object : OtelJavaBatchCallback {
    override fun close() = Unit
}
