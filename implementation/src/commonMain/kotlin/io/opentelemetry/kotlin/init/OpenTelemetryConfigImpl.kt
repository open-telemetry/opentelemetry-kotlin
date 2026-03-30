package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock

internal class OpenTelemetryConfigImpl(
    clock: Clock,
    private val globalResourceConfig: ResourceConfigImpl = ResourceConfigImpl(),
) : OpenTelemetryConfigDsl, ResourceConfigDsl by globalResourceConfig {

    internal val tracingConfig: TracerProviderConfigImpl = TracerProviderConfigImpl(clock)
    internal val loggingConfig: LoggerProviderConfigImpl = LoggerProviderConfigImpl(clock)
    internal val contextConfig: ContextConfigImpl = ContextConfigImpl()
    private val globalAttributeLimits = AttributeLimitsConfigImpl()

    override fun attributeLimits(action: AttributeLimitsConfigDsl.() -> Unit) {
        globalAttributeLimits.action()
    }

    override fun tracerProvider(action: TracerProviderConfigDsl.() -> Unit) {
        tracingConfig.action()
    }

    override fun loggerProvider(action: LoggerProviderConfigDsl.() -> Unit) {
        loggingConfig.action()
    }

    override fun context(action: ContextConfigDsl.() -> Unit) {
        contextConfig.action()
    }

    private val defaultResource by lazy(::sdkDefaultResource)

    internal fun generateTracingConfig() =
        tracingConfig.generateTracingConfig(defaultResource.merge(globalResourceConfig.generateResource()), globalAttributeLimits)

    internal fun generateLoggingConfig() =
        loggingConfig.generateLoggingConfig(defaultResource.merge(globalResourceConfig.generateResource()), globalAttributeLimits)
}
