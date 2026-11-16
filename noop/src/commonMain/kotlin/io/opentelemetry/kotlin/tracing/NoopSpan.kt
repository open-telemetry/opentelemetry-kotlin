package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
internal object NoopSpan : Span by createNonRecordingSpan(createInvalidSpanContext(), createInvalidSpanContext())
