package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.SdkFactory

@ExperimentalApi
internal class CompatOpenTelemetryConfig(
    clock: Clock,
    sdkFactory: SdkFactory,
) : OpenTelemetryConfigDsl {

    internal val tracerProviderConfig = CompatTracerProviderConfig(clock, sdkFactory)
    internal val loggerProviderConfig = CompatLoggerProviderConfig(clock)

    override fun context(action: ContextConfigDsl.() -> Unit) {
        // no-op
    }

    override fun tracerProvider(action: TracerProviderConfigDsl.() -> Unit) {
        tracerProviderConfig.action()
    }

    override fun loggerProvider(action: LoggerProviderConfigDsl.() -> Unit) {
        loggerProviderConfig.action()
    }
}
