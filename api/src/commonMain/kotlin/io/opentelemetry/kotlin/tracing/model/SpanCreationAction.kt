package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.TracingDsl

/**
 * Allows attributes, links, and events to be configured at span creation time.
 */
@ExperimentalApi
@TracingDsl
public interface SpanCreationAction : MutableAttributeContainer, SpanLinkMutator, SpanEventMutator
