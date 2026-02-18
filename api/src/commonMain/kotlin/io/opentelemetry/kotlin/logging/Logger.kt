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
     * Returns whether a log record should be emitted based on the provided parameters.
     *
     * This method allows callers to avoid the cost of creating log records that will be dropped by
     * returning false if a log shouldn't be created.
     *
     * @param context The context associated with the log record. If null, the implicit context is used.
     * @param severityNumber The severity of the log record (optional)
     * @param eventName The event name of the log record (optional)
     * @return true if a log record should be emitted
     */
    public fun enabled(
        context: Context? = null,
        severityNumber: SeverityNumber? = null,
        eventName: String? = null,
    ): Boolean

    @Deprecated(
        "Deprecated",
        ReplaceWith(
            expression = "emit(body, eventName, timestamp, observedTimestamp, context, severityNumber, severityText, attributes)",
            imports = ["io.opentelemetry.kotlin.logging.model.SeverityNumber"]
        )
    )
    public fun log(
        body: String? = null,
        timestamp: Long? = null,
        observedTimestamp: Long? = null,
        context: Context? = null,
        severityNumber: SeverityNumber? = null,
        severityText: String? = null,
        attributes: (MutableAttributeContainer.() -> Unit)? = null,
    )

    @Deprecated(
        "Deprecated",
        ReplaceWith(
            expression = "emit(body, eventName, timestamp, observedTimestamp, context, severityNumber, severityText, attributes)",
            imports = ["io.opentelemetry.kotlin.logging.model.SeverityNumber"]
        )
    )
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

    /**
     * Emits an event with a name and the given optional parameters:
     *
     * - [body] - the body of the log message
     * - [eventName] - the name of the event, or null if none is set
     * - [timestamp] - the timestamp at which the event occurred
     * - [observedTimestamp] - the timestamp at which the event was entered into the OpenTelemetry API
     * - [context] - the context in which the log was emitted
     * - [severityNumber] - the severity of the log
     * - [severityText] - a string representation of the severity at the point it was captured
     * - [attributes] - additional attributes to associate with the log
     */
    public fun emit(
        body: String? = null,
        eventName: String? = null,
        timestamp: Long? = null,
        observedTimestamp: Long? = null,
        context: Context? = null,
        severityNumber: SeverityNumber? = null,
        severityText: String? = null,
        attributes: (MutableAttributeContainer.() -> Unit)? = null,
    )
}
