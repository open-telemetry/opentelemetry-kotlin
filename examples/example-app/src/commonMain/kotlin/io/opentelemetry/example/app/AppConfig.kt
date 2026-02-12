package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.createSimpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.createStdoutLogRecordExporter
import io.opentelemetry.kotlin.tracing.export.createSimpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.createStdoutSpanExporter

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
     * The [io.opentelemetry.kotlin.tracing.export.SpanProcessor] that should be used to export
     * telemetry. The example app defaults to logging immediately to stdout. In a real application,
     * you would likely use [io.opentelemetry.kotlin.tracing.export.createBatchSpanProcessor] with
     * an OTLP span exporter.
     */
    val spanProcessor = createSimpleSpanProcessor(createStdoutSpanExporter())

    /**
     * The [io.opentelemetry.kotlin.logging.export.LogRecordProcessor] that should be used to export
     * telemetry. The example app defaults to logging immediately to stdout. In a real application,
     * you would likely use [io.opentelemetry.kotlin.logging.export.createBatchLogRecordProcessor] with
     * an OTLP log record exporter.
     */
    val logRecordProcessor = createSimpleLogRecordProcessor(createStdoutLogRecordExporter())
}
