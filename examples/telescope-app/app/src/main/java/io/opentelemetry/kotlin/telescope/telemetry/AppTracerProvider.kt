package io.opentelemetry.kotlin.telescope.telemetry

import android.content.res.Resources
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.toOtelKotlinApi
import io.opentelemetry.kotlin.tracing.Tracer
import io.opentelemetry.exporter.logging.LoggingSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes

@OptIn(ExperimentalApi::class)
class AppTracerProvider(
    private val resources: Resources,
    private val serviceName: String = "My telescopes"
) {
    val tracer: Tracer by lazy { createTracer() }

    private fun createTracer(): Tracer {
        val loggingExporter = LoggingSpanExporter.create()

        val resource = Resource.getDefault().toBuilder()
            .put(ServiceAttributes.SERVICE_NAME, serviceName)
            .put("device.screen_resolution", getScreenResolution())
            .build()

        val sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(loggingExporter).build())
            .setResource(resource)
            .build()

        val javaSdk = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .build()

        val kotlinApi = javaSdk.toOtelKotlinApi()

        return kotlinApi.tracerProvider.getTracer("AppTracer")
    }

    private fun getScreenResolution() = "${resources.displayMetrics.widthPixels}x${resources.displayMetrics.heightPixels}"
}