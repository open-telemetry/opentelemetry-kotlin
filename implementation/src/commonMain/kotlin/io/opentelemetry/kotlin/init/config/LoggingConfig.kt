package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.resource.Resource

/**
 * Configuration for the Logging API.
 */
@ThreadSafe
internal class LoggingConfig(

    /**
     * The processor to use for log record data.
     */
    val processor: LogRecordProcessor?,

    /**
     * Limits on log data capture.
     */
    val logLimits: LogLimitConfig,

    /**
     * A resource to append to spans.
     */
    val resource: Resource
)
