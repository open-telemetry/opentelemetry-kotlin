package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Processor used by the logger provider.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecordprocessor
 */
@ExperimentalApi
class LogRecordProcessorBehavior : Behavior<LogRecordProcessorBehavior> {

    override fun mergeWith(higher: LogRecordProcessorBehavior): LogRecordProcessorBehavior = higher

    override fun equals(other: Any?): Boolean = other is LogRecordProcessorBehavior

    override fun hashCode(): Int = 0
}
