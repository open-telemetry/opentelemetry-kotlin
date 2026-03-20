package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.Span

/**
 * A writable representation of a [io.opentelemetry.kotlin.tracing.Span] that can be modified.
 */
@ExperimentalApi
public interface ReadWriteSpan : Span, ReadableSpan
