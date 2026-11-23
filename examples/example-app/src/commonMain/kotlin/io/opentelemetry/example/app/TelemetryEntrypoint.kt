package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

/**
 * Initializes the OpenTelemetry SDK. Telemetry is exported to [url] via OTLP/HTTP if set,
 * otherwise it is printed to stdout.
 */
@OptIn(ExperimentalApi::class)
fun initializeOtelSdk(url: String? = AppConfig.url): OpenTelemetrySdk {
    val config: OpenTelemetryConfigDsl.() -> Unit = {
        tracerProvider {
            export { createSpanProcessor(url) }
        }
        loggerProvider {
            export { createLogRecordProcessor(url) }
        }
    }
    return createPlatformOpenTelemetry(AppConfig.sdkMode, config)
}
