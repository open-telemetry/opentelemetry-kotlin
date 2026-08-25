package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.config.envar.processor.EnvVarConfigProcessor
import io.opentelemetry.kotlin.init.config.SpanLimitConfig

internal abstract class SpanLimitEnvVarConfigProcessor : EnvVarConfigProcessor<SpanLimitConfig, Int>()
