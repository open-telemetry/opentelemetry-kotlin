package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider

class OpenTelemetryImpl(
    override val tracerProvider: TracerProvider,
    override val loggerProvider: LoggerProvider,
    override val clock: Clock,
    override val idGenerator: IdGenerator,
    private val sdkFactory: SdkFactory
) : OpenTelemetrySdk, SdkFactory by sdkFactory
