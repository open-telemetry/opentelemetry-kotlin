package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

/**
 * Initializes the OpenTelemetry SDK. Telemetry is exported to [url] via OTLP/HTTP.
 */
@OptIn(ExperimentalApi::class)
fun initializeOtelSdk(url: String = AppConfig.url): OpenTelemetrySdk {
    val config: OpenTelemetryConfigDsl.() -> Unit = {
        serviceName = AppConfig.SERVICE_NAME
        tracerProvider {
            export { createSpanProcessor(url) }
        }
        loggerProvider {
            export { createLogRecordProcessor(url) }
        }
    }
    return createPlatformOpenTelemetry(AppConfig.sdkMode, config)
}
