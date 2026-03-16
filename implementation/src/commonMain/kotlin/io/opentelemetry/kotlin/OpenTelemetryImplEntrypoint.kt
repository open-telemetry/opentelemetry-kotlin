package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.logging.LoggerProviderImpl
import io.opentelemetry.kotlin.tracing.TracerProviderImpl

/**
 * Constructs an [OpenTelemetry] instance that uses the opentelemetry-kotlin implementation.
 */
@ExperimentalApi
public fun createOpenTelemetry(

    /**
     * Defines the [Clock] implementation used by OpenTelemetry.
     */
    clock: Clock = ClockImpl(),

    /**
     * Defines configuration for OpenTelemetry.
     */
    config: OpenTelemetryConfigDsl.() -> Unit = {}
): OpenTelemetry {
    return createOpenTelemetryImpl(clock, config)
}

/**
 * Internal implementation of [createOpenTelemetry]. This is not publicly visible as
 * we don't want to allow users to supply a custom [IdGenerator].
 */
@ExperimentalApi
internal fun createOpenTelemetryImpl(
    clock: Clock,
    config: OpenTelemetryConfigDsl.() -> Unit,
    idGenerator: IdGenerator = IdGeneratorImpl(),
): OpenTelemetry {
    val resourceFactory = ResourceFactoryImpl()
    val traceFlags = TraceFlagsFactoryImpl()
    val traceState = TraceStateFactoryImpl()
    val spanContext = SpanContextFactoryImpl(idGenerator, traceFlags, traceState)
    val contextFactory = ContextFactoryImpl()
    val span = SpanFactoryImpl(spanContext, contextFactory.spanKey)

    val cfg = OpenTelemetryConfigImpl(clock).apply(config)
    val tracingConfig = cfg.tracingConfig.generateTracingConfig()
    val loggingConfig = cfg.loggingConfig.generateLoggingConfig()
    return OpenTelemetryImpl(
        tracerProvider = TracerProviderImpl(
            clock = clock,
            tracingConfig = tracingConfig,
            contextFactory = contextFactory,
            spanContextFactory = spanContext,
            traceFlagsFactory = traceFlags,
            traceStateFactory = traceState,
            spanFactory = span,
            idGenerator = idGenerator,
        ),
        loggerProvider = LoggerProviderImpl(
            clock = clock,
            loggingConfig = loggingConfig,
            contextFactory = contextFactory,
            spanContextFactory = spanContext,
            spanFactory = span,
        ),
        clock = clock,
        spanContext = spanContext,
        traceFlags = traceFlags,
        traceState = traceState,
        context = contextFactory,
        span = span,
        idGenerator = idGenerator,
        resource = resourceFactory,
    )
}
