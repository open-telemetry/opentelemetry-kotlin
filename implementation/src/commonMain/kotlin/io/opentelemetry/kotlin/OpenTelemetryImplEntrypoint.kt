package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.config.ConfigProviderImpl
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.installContextStorage
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.init.SdkConfigFactory
import io.opentelemetry.kotlin.init.defaultBehaviorReader
import io.opentelemetry.kotlin.logging.LoggerProviderImpl
import io.opentelemetry.kotlin.metrics.MeterProviderImpl
import io.opentelemetry.kotlin.tracing.TracerProviderImpl

/**
 * Constructs an [OpenTelemetrySdk] instance that uses the opentelemetry-kotlin implementation.
 *
 * The caller owns the returned instance and should call [OpenTelemetrySdk.shutdown] when telemetry
 * is no longer needed.
 */
@ExperimentalApi
public fun createOpenTelemetry(

    /**
     * Defines configuration for OpenTelemetry.
     */
    config: OpenTelemetryConfigDsl.() -> Unit = {}
): OpenTelemetrySdk {
    val cfg = OpenTelemetryConfigImpl()
    cfg.sdkErrorHandler.guard("Failed to apply OpenTelemetry config, continuing with partial config") {
        cfg.config()
    }
    val clock = cfg.clock
    val resourceFactory = ResourceFactoryImpl(cfg.sdkErrorHandler)
    val behaviorReader = defaultBehaviorReader(
        sdkErrorHandler = cfg.sdkErrorHandler,
        declarativeConfigReader = cfg.declarativeConfigReader,
    )
    val configFilePath = cfg.configFilePath
    val behavior = behaviorReader.read(configFilePath = configFilePath, dsl = cfg::toBehavior)

    if (behavior.disabled == true) {
        return NoopOpenTelemetrySdk
    }

    // configFactory is legacy - use behavior to control SDK functionality instead
    val configFactory = SdkConfigFactory(cfg, behavior, resourceFactory)
    val idGenerator = configFactory.idGenerator

    val tracingConfig = configFactory.generateTracingConfig()
    val loggingConfig = configFactory.generateLoggingConfig()
    val metricsConfig = configFactory.generateMetricsConfig()

    // Claim process-wide state last, so that a failure earlier in init doesn't leave it claimed.
    val installedStorage = installContextStorage(cfg)
    val ownsErrorSink = setProcessSdkErrorHandler(cfg.sdkErrorHandler)
    val contextFactory = ContextFactoryImpl(cfg.sdkErrorHandler, installedStorage)
    return OpenTelemetryImpl(
        tracerProvider = TracerProviderImpl(
            clock = clock,
            tracingConfig = tracingConfig,
            idGenerator = idGenerator,
            attributeLimits = behavior.attributeLimits ?: AttributeLimitsBehavior(),
            spanLimits = behavior.tracerProvider?.spanLimits ?: SpanLimitsBehavior(),
        ),
        loggerProvider = LoggerProviderImpl(
            clock = clock,
            loggingConfig = loggingConfig,
            attributeLimits = behavior.attributeLimits ?: AttributeLimitsBehavior(),
            logLimits = behavior.loggerProvider?.logLimits ?: LogLimitsBehavior(),
        ),
        meterProvider = MeterProviderImpl(
            metricsConfig = metricsConfig,
            attributeLimits = behavior.attributeLimits ?: AttributeLimitsBehavior()
        ),
        configProvider = ConfigProviderImpl { behaviorReader.readInstrumentation(configFilePath) },
        clock = clock,
        context = contextFactory,
        idGenerator = idGenerator,
        resource = resourceFactory,
        propagator = cfg.propagator,
        installedStorage = installedStorage,
        ownsErrorSink = ownsErrorSink,
        timeoutMs = cfg.shutdownTimeoutMs,
        sdkErrorHandler = cfg.sdkErrorHandler,
    )
}

/**
 * Installs the configured storage (or the single-slot default) as the process-wide storage.
 * If another storage is already installed it is used instead, and a warning is reported only if
 * a storage was explicitly configured.
 */
private fun installContextStorage(cfg: OpenTelemetryConfigImpl): CurrentContextStorage? {
    val contextConfig = cfg.contextConfig
    val storage = contextConfig.generateStorage()
    if (installContextStorage(storage)) {
        return storage
    }
    if (contextConfig.explicitlyConfigured) {
        cfg.sdkErrorHandler.reportStorageAlreadyInstalled()
    }
    return null
}

private fun SdkErrorHandler.reportStorageAlreadyInstalled() {
    onError(
        SdkError.ApiMisuse(
            api = "context",
            message = "A CurrentContextStorage is already installed for this process, so the " +
                "configured storage is ignored and the installed one is used instead.",
            severity = SdkErrorSeverity.WARNING,
        )
    )
}
