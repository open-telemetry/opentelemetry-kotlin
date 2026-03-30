package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter

public fun OtelJavaSpanContext.toOtelKotlinSpanContext(): SpanContext = SpanContextAdapter(this)
