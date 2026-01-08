package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ClockImpl
import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class OpenTelemetryConfigImpl : OpenTelemetryConfigDsl {

    override var clock: Clock = ClockImpl()

    internal val tracingConfig: TracerProviderConfigImpl = TracerProviderConfigImpl()
    internal val loggingConfig: LoggerProviderConfigImpl = LoggerProviderConfigImpl()
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
