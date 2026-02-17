package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class OpenTelemetryConfigImpl(clock: Clock) : OpenTelemetryConfigDsl {

    internal val tracingConfig: TracerProviderConfigImpl = TracerProviderConfigImpl(clock)
    internal val loggingConfig: LoggerProviderConfigImpl = LoggerProviderConfigImpl(clock)
    internal val contextConfig: ContextConfigImpl = ContextConfigImpl()

    override fun tracerProvider(action: TracerProviderConfigDsl.() -> Unit) {
        tracingConfig.action()
    }

    override fun loggerProvider(action: LoggerProviderConfigDsl.() -> Unit) {
        loggingConfig.action()
    }

    override fun context(action: ContextConfigDsl.() -> Unit) {
        contextConfig.action()
    }
}
