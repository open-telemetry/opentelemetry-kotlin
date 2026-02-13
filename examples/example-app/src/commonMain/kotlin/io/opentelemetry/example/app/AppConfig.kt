package io.opentelemetry.example.app

import io.opentelemetry.example.app.AppConfig.url
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

/**
 * Controls how the example app behaves.
 */
@OptIn(ExperimentalApi::class)
object AppConfig {

    /**
     * Which implementation the SDK should use to capture telemetry.
     */
    enum class SdkMode {

        /**
         * KMP implementation of OpenTelemetry. This is the default.
         */
        IMPLEMENTATION,

        /**
         * Kotlin API that wraps the opentelemetry-java SDK.
         * Only available on JVM/Android platforms.
         */
        COMPAT,

        /**
         * No-op implementation (KMP).
         */
        NOOP
    }

    /**
     * Name of the application.
     */
    const val APP_NAME = "example-app"

    /**
     * The implementation of the Kotlin API that should be used to capture telemetry.
     */
    val sdkMode = SdkMode.IMPLEMENTATION

    /**
     * URL of an OpenTelemetry collector endpoint. If set, telemetry will be exported
     * to the collector via OTLP/HTTP, otherwise telemetry will be printed to stdout.
     */
    val url: String? = null

    /**
     * The [io.opentelemetry.kotlin.tracing.export.SpanProcessor] that should be used to export
     * telemetry. If [url] is set, an OTLP HTTP exporter with batch processing will be used.
     * Otherwise, telemetry is logged immediately to stdout.
     */
    val spanProcessor: SpanProcessor = createSpanProcessor(url)

    /**
     * The [io.opentelemetry.kotlin.logging.export.LogRecordProcessor] that should be used to export
     * telemetry. If [url] is set, an OTLP HTTP exporter with batch processing will be used.
     * Otherwise, telemetry is logged immediately to stdout.
     */
    val logRecordProcessor: LogRecordProcessor = createLogRecordProcessor(url)
}
