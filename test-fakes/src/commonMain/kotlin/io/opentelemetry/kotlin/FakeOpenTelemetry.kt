package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeResourceFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.factory.FakeTraceStateFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.logging.FakeLoggerProvider
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.FakeTracerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider

class FakeOpenTelemetry : OpenTelemetrySdk {
    override val tracerProvider: TracerProvider = FakeTracerProvider()
    override val loggerProvider: LoggerProvider = FakeLoggerProvider()
    override val clock: Clock = FakeClock()
    override val spanContext: SpanContextFactory = FakeSpanContextFactory()
    override val traceFlags: TraceFlagsFactory = FakeTraceFlagsFactory()
    override val traceState: TraceStateFactory = FakeTraceStateFactory()
    override val context: ContextFactory = FakeContextFactory()
    override val span: SpanFactory = FakeSpanFactory()
    override val idGenerator: IdGenerator = FakeIdGenerator()
    override val resource: ResourceFactory = FakeResourceFactory()
}
