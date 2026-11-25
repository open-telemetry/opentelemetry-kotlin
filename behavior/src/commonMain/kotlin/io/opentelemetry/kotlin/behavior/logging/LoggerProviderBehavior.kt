package io.opentelemetry.kotlin.behavior.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Behavior of logging.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#loggerprovider
 */
@OptIn(ExperimentalApi::class)
data class LoggerProviderBehavior(

    /**
     * Limits on log record data capture.
     */
    val logRecordLimits: LogRecordLimitsBehavior? = null,

    /**
     * Processor used by the logger provider.
     */
    val processor: LogRecordProcessorBehavior? = null,

) : Behavior<LoggerProviderBehavior> {

    override fun mergeWith(higher: LoggerProviderBehavior): LoggerProviderBehavior = copy(
        logRecordLimits = mergeNode(logRecordLimits, higher.logRecordLimits),
        processor = mergeNode(processor, higher.processor),
    )
}
