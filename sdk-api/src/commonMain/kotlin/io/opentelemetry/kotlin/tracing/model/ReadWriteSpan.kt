package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer

/**
 * A writable representation of a [Span] that can be modified.
 */
@ExperimentalApi
public interface ReadWriteSpan : Span, ReadableSpan, AttributeContainer
