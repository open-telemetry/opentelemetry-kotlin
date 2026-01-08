package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.logging.FakeLoggerProvider
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.FakeTracerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider

@OptIn(ExperimentalApi::class)
class FakeOpenTelemetry(
    private val sdkFactory: SdkFactory = FakeSdkFactory()
) : OpenTelemetry, SdkFactory by sdkFactory {
    override val tracerProvider: TracerProvider = FakeTracerProvider()
    override val loggerProvider: LoggerProvider = FakeLoggerProvider()
    override val clock: Clock = FakeClock()
}
