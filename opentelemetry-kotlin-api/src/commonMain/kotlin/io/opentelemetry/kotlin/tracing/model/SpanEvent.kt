package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.TracingDsl
import io.opentelemetry.kotlin.tracing.data.EventData

/**
 * Represents an event that happened on a span
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#add-events
 */
@TracingDsl
@ExperimentalApi
@ThreadSafe
public interface SpanEvent : EventData, MutableAttributeContainer
