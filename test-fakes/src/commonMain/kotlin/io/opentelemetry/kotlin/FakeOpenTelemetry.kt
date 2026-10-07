package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeResourceFactory
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.logging.FakeLoggerProvider
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.metrics.FakeMeterProvider
import io.opentelemetry.kotlin.metrics.MeterProvider
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.tracing.FakeTracerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider

class FakeOpenTelemetry : OpenTelemetrySdk {
    override val tracerProvider: TracerProvider = FakeTracerProvider()
    override val loggerProvider: LoggerProvider = FakeLoggerProvider()
    override val meterProvider: MeterProvider = FakeMeterProvider()
    override val clock: Clock = FakeClock()
    override val context: ContextFactory = FakeContextFactory()
    override val span: SpanFactory = FakeSpanFactory()
    override val idGenerator: IdGenerator = FakeIdGenerator()
    override val resource: ResourceFactory = FakeResourceFactory()
    override val propagator: TextMapPropagator = FakeTextMapPropagator()

    var forceFlushCount: Int = 0
        private set

    var shutdownCount: Int = 0
        private set

    override suspend fun forceFlush(): OperationResultCode {
        forceFlushCount++
        return OperationResultCode.Success
    }

    override suspend fun shutdown(): OperationResultCode {
        shutdownCount++
        return OperationResultCode.Success
    }
}
