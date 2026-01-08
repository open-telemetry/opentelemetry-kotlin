package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter

@OptIn(ExperimentalApi::class)
public fun OtelJavaSpanContext.toOtelKotlinSpanContext(): SpanContext = SpanContextAdapter(this)
