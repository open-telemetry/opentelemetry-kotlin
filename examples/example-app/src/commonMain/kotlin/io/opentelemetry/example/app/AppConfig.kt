package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

/**
 * Controls how the example app behaves.
 */
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

    @ExperimentalApi
    internal var spanProcessor: SpanProcessor? = null

    @ExperimentalApi
    internal var logRecordProcessor: LogRecordProcessor? = null

    @ExperimentalApi
    suspend fun forceFlush() {
        spanProcessor?.forceFlush()
        logRecordProcessor?.forceFlush()
    }

    @ExperimentalApi
    suspend fun shutdown() {
        spanProcessor?.shutdown()
        logRecordProcessor?.shutdown()
    }
}
