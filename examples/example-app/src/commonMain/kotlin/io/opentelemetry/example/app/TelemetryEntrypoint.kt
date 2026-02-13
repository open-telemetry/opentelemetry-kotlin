@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

/**
 * Initializes the OpenTelemetry SDK.
 */
fun initializeOtelSdk(): OpenTelemetry {
    val config: OpenTelemetryConfigDsl.() -> Unit = {
        tracerProvider {
            addSpanProcessor(AppConfig.spanProcessor)
        }
        loggerProvider {
            addLogRecordProcessor(AppConfig.logRecordProcessor)
        }
    }
    return createPlatformOpenTelemetry(AppConfig.sdkMode, config)
}
