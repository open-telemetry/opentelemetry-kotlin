package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.resource.Resource

/**
 * Configuration for the Logging API.
 */
@OptIn(ExperimentalApi::class)
@ThreadSafe
internal class LoggingConfig(

    /**
     * List of processors. These will be executed in the order they are provided.
     */
    val processors: List<LogRecordProcessor>,

    /**
     * Limits on log data capture.
     */
    val logLimits: LogLimitConfig,

    /**
     * A resource to append to spans.
     */
    val resource: Resource
)
