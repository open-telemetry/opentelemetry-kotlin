package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider

@OptIn(ExperimentalApi::class)
class OpenTelemetryImpl(
    override val tracerProvider: TracerProvider,
    override val loggerProvider: LoggerProvider,
    override val clock: Clock,
    private val sdkFactory: SdkFactory
) : OpenTelemetry, SdkFactory by sdkFactory
