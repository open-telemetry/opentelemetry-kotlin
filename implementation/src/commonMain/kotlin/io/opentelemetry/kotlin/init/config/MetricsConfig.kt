package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.resource.Resource

/**
 * Configuration for the Metrics API.
 */
@ThreadSafe
internal class MetricsConfig(

    /**
     * A resource to append to metrics.
     */
    val resource: Resource,
)
