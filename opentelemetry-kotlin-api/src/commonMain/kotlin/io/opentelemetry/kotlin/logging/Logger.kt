package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.logging.model.SeverityNumber

/**
 * Class that emits log record objects.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/api/#logger
 */
@ExperimentalApi
@ThreadSafe
public interface Logger {

    /**
     * Emits a log record with the given optional parameters:
     *
     * - [body] - the body of the log message
     * - [timestamp] - the timestamp at which the event occurred
     * - [observedTimestamp] - the timestamp at which the event was entered into the OpenTelemetry API
     * - [context] - the context in which the log was emitted
     * - [severityNumber] - the severity of the log
     * - [severityText] - a string representation of the severity at the point it was captured
     * - [attributes] - additional attributes to associate with the log
     */
    public fun log(
        body: String? = null,
        timestamp: Long? = null,
        observedTimestamp: Long? = null,
        context: Context? = null,
        severityNumber: SeverityNumber? = null,
        severityText: String? = null,
        attributes: (MutableAttributeContainer.() -> Unit)? = null,
    )

    /**
     * Emits an event with a name and the given optional parameters:
     *
     * - [body] - the body of the log message
     * - [timestamp] - the timestamp at which the event occurred
     * - [observedTimestamp] - the timestamp at which the event was entered into the OpenTelemetry API
     * - [context] - the context in which the log was emitted
     * - [severityNumber] - the severity of the log
     * - [severityText] - a string representation of the severity at the point it was captured
     * - [attributes] - additional attributes to associate with the log
     */
    public fun logEvent(
        eventName: String,
        body: String? = null,
        timestamp: Long? = null,
        observedTimestamp: Long? = null,
        context: Context? = null,
        severityNumber: SeverityNumber? = null,
        severityText: String? = null,
        attributes: (MutableAttributeContainer.() -> Unit)? = null,
    )
}
