package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * A writable representation of a [Span] that can be modified.
 */
@ExperimentalApi
public interface ReadWriteSpan : Span, ReadableSpan
