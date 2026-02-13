package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

/**
 * Creates an OpenTelemetry instance using platform-specific implementation.
 * On JVM/Android, this respects [sdkMode] to choose between
 * KMP SDK, Java SDK (via compat module), or no-op.
 * On other platforms, COMPAT mode is not supported and will throw an exception.
 */
@ExperimentalApi
internal expect fun createPlatformOpenTelemetry(
    sdkMode: AppConfig.SdkMode,
    config: OpenTelemetryConfigDsl.() -> Unit
): OpenTelemetry
