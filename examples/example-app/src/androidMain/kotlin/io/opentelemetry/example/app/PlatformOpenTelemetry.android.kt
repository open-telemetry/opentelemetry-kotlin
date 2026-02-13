package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

@ExperimentalApi
internal actual fun createPlatformOpenTelemetry(
    sdkMode: AppConfig.SdkMode,
    config: OpenTelemetryConfigDsl.() -> Unit
): OpenTelemetry {
    return when (sdkMode) {
        AppConfig.SdkMode.IMPLEMENTATION -> createOpenTelemetry(config)
        AppConfig.SdkMode.COMPAT -> createCompatOpenTelemetry(config)
        AppConfig.SdkMode.NOOP -> NoopOpenTelemetry
    }
}
