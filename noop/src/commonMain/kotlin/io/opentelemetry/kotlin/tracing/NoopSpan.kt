package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal object NoopSpan : Span by createNonRecordingSpan(NoopSpanContext, NoopSpanContext)
