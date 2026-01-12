package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanProcessor

/**
 * Converts an opentelemetry-java span processor to an opentelemetry-kotlin span processor.
 * This is useful if you wish to use an existing Java processor whilst using opentelemetry-kotlin.
 */
@OptIn(ExperimentalApi::class)
public fun OtelJavaSpanProcessor.toOtelKotlinSpanProcessor(): SpanProcessor =
    SpanProcessorAdapter(this)
