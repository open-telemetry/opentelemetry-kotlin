package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetrySdk
import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

@OptIn(ExperimentalApi::class)
internal actual fun createPlatformOpenTelemetry(
    sdkMode: AppConfig.SdkMode,
    config: OpenTelemetryConfigDsl.() -> Unit
): OpenTelemetrySdk {
    return when (sdkMode) {
        AppConfig.SdkMode.IMPLEMENTATION -> createOpenTelemetry(config = config)
        AppConfig.SdkMode.COMPAT -> {
            throw UnsupportedOperationException(
                "COMPAT mode is unsupported on JS. Use IMPLEMENTATION or NOOP instead."
            )
        }

        AppConfig.SdkMode.NOOP -> NoopOpenTelemetrySdk
    }
}
