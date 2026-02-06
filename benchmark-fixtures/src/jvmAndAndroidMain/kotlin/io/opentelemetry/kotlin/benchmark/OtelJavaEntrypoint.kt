package io.opentelemetry.kotlin.benchmark

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk

fun createOtelJavaOpenTelemetry(): OtelJavaOpenTelemetry = OtelJavaOpenTelemetrySdk.builder().build()
