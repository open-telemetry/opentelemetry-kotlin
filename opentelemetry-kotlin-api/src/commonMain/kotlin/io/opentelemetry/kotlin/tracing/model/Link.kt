package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.data.LinkData

/**
 * Represents a link to a [SpanContext] and optional attributes further describing the link.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#link
 */
@ExperimentalApi
@ThreadSafe
public interface Link : LinkData, MutableAttributeContainer
