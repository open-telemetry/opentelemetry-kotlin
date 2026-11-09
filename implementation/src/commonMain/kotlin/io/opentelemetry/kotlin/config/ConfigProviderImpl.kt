package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.NoopOpenTelemetry

/**
 * Reads the instrumentation config on first access rather than when the SDK is initialized.
 */
internal class ConfigProviderImpl(read: () -> ConfigProperties?) : ConfigProvider {
    override val instrumentationConfig: ConfigProperties by lazy {
        read() ?: NoopOpenTelemetry.configProvider.instrumentationConfig
    }
}
