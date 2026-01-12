package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.model.SpanKind

@OptIn(ExperimentalApi::class)
internal fun OtelJavaSpanKind.toOtelKotlinSpanKind(): SpanKind = when (this) {
    OtelJavaSpanKind.INTERNAL -> SpanKind.INTERNAL
    OtelJavaSpanKind.CLIENT -> SpanKind.CLIENT
    OtelJavaSpanKind.SERVER -> SpanKind.SERVER
    OtelJavaSpanKind.PRODUCER -> SpanKind.PRODUCER
    OtelJavaSpanKind.CONSUMER -> SpanKind.CONSUMER
}
