
package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

/**
 * Initializes the OpenTelemetry SDK.
 */
@OptIn(ExperimentalApi::class)
fun initializeOtelSdk(): OpenTelemetry {
    val config: OpenTelemetryConfigDsl.() -> Unit = {
        tracerProvider {
            export { createSpanProcessor(AppConfig.url).also { AppConfig.spanProcessor = it } }
        }
        loggerProvider {
            export { createLogRecordProcessor(AppConfig.url).also { AppConfig.logRecordProcessor = it } }
        }
    }
    return createPlatformOpenTelemetry(AppConfig.sdkMode, config)
}
