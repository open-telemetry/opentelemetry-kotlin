package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

@ExperimentalApi
internal actual fun createPlatformOpenTelemetry(
    sdkMode: AppConfig.SdkMode,
    config: OpenTelemetryConfigDsl.() -> Unit
): OpenTelemetry {
    return when (sdkMode) {
        AppConfig.SdkMode.IMPLEMENTATION -> createOpenTelemetry(config = config)
        AppConfig.SdkMode.COMPAT -> {
            throw UnsupportedOperationException(
                "COMPAT mode is unsupported on JS. Use IMPLEMENTATION or NOOP instead."
            )
        }

        AppConfig.SdkMode.NOOP -> NoopOpenTelemetry
    }
}
