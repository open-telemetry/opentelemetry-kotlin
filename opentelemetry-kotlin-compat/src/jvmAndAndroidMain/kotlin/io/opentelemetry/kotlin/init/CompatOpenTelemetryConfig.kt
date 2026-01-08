package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.factory.SdkFactory

@ExperimentalApi
internal class CompatOpenTelemetryConfig(
    sdkFactory: SdkFactory,
    override var clock: Clock = ClockAdapter(io.opentelemetry.sdk.common.Clock.getDefault())
) : OpenTelemetryConfigDsl {

    internal val tracerProviderConfig = CompatTracerProviderConfig(sdkFactory)
    internal val loggerProviderConfig = CompatLoggerProviderConfig()

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
