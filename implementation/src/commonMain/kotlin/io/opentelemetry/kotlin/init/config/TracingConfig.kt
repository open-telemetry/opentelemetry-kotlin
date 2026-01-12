package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

/**
 * Configuration for the Tracing API.
 */
@OptIn(ExperimentalApi::class)
@ThreadSafe
internal class TracingConfig(

    /**
     * List of processors. These will be executed in the order they are provided.
     */
    val processors: List<SpanProcessor>,

    /**
     * Limits on span data capture.
     */
    val spanLimits: SpanLimitConfig,

    /**
     * A resource to append to spans.
     */
    val resource: Resource
)
