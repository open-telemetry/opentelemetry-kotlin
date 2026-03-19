package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * Allows attributes and links to be configured at span creation time.
 */
@ExperimentalApi
@TracingDsl
public interface SpanCreationAction : AttributesMutator, SpanLinkCreator
