package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Unbuffered processing for the logger provider's processor: each log record is exported as it is
 * emitted. The exporter is configured on [LogRecordProcessorBehavior]. This type has no fields;
 * selecting it is the whole configuration.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#simple-processor
 */
@ExperimentalApi
class SimpleLogRecordProcessorBehavior : Behavior<SimpleLogRecordProcessorBehavior> {

    override fun mergeWith(higher: SimpleLogRecordProcessorBehavior): SimpleLogRecordProcessorBehavior = higher

    override fun equals(other: Any?): Boolean = other is SimpleLogRecordProcessorBehavior

    override fun hashCode(): Int = 0
}
