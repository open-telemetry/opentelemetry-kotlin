package io.opentelemetry.api.trace

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

internal val otelJavaSpanContextKey: OtelJavaContextKey<Span> = SpanContextKey.KEY
