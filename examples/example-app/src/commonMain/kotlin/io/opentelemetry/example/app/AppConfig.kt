package io.opentelemetry.example.app

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
     * The `service.name` resource attribute attached to all telemetry.
     */
    const val SERVICE_NAME = "otel-kotlin-example"

    /**
     * The implementation of the Kotlin API that should be used to capture telemetry.
     */
    val sdkMode = SdkMode.IMPLEMENTATION

    /**
     * Base URL of the OTLP/HTTP endpoint that telemetry is exported to. Defaults to
     * `examples/local-collector` as seen from the current platform.
     */
    val url: String = defaultCollectorUrl
}
