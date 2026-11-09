package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

/**
 * Initializes the OpenTelemetry SDK.
 */
@OptIn(ExperimentalApi::class)
fun initializeOtelSdk(): OpenTelemetrySdk {
    val config: OpenTelemetryConfigDsl.() -> Unit = {
        tracerProvider {
            export { createSpanProcessor(AppConfig.url) }
        }
        loggerProvider {
            export { createLogRecordProcessor(AppConfig.url) }
        }
    }
    return createPlatformOpenTelemetry(AppConfig.sdkMode, config)
}
