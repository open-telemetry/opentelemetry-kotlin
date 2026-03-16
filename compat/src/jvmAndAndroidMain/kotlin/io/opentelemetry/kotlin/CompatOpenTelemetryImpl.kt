package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider

internal class CompatOpenTelemetryImpl(
    override val tracerProvider: TracerProvider,
    override val loggerProvider: LoggerProvider,
    override val clock: Clock,
    override val spanContext: SpanContextFactory,
    override val traceFlags: TraceFlagsFactory,
    override val traceState: TraceStateFactory,
    override val context: ContextFactory,
    override val span: SpanFactory,
    override val idGenerator: IdGenerator,
    override val resource: ResourceFactory,
) : OpenTelemetrySdk
