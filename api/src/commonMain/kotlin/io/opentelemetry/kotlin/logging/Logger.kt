package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

/**
 * Class that emits log record objects.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/api/#logger
 */
@ThreadSafe
public interface Logger {

    /**
     * Returns whether a log record should be emitted based on the provided parameters.
     *
     * This method allows callers to avoid the cost of creating log records that will be dropped by
     * returning false if a log shouldn't be created. This should be invoked each time before
     * calling [emit] to ensure an up-to-date response.
     *
     * @param context The context associated with the log record. If null, the current context is used.
     * @param severityNumber The severity of the log record (optional)
     * @param eventName The event name of the log record (optional)
     * @return true if a log record should be emitted
     */
    @OptIn(ExperimentalApi::class)
    public fun enabled(
        context: Context? = null,
        severityNumber: SeverityNumber? = null,
        eventName: String? = null,
    ): Boolean

    /**
     * Emits an event with a name and the given optional parameters:
     *
     * - [body] - the body of the log message. Pass, [String] for a simple textual body,
     *   [io.opentelemetry.kotlin.attributes.AnyValue] for structured body types, and any other
     *   type that will be converted via toString().
     * - [eventName] - the name of the event, or null if none is set
     * - [timestamp] - the time the event occurred, in nanoseconds since the Unix epoch. Defaults to 0, which
     *   means unknown; the timestamp is then left unset. Any value <= 0 is treated as unknown.
     * - [observedTimestamp] - the time the event was entered into the OpenTelemetry API, in nanoseconds since
     *   the Unix epoch. Defaults to 0, which means the current time. Any value <= 0 is treated as the current time.
     * - [context] - the context in which the log was emitted
     * - [severityNumber] - the severity of the log
     * - [severityText] - a string representation of the severity at the point it was captured
     * - [exception] - an optional exception to associate with the log record
     * - [attributes] - additional attributes to associate with the log
     */
    @OptIn(ExperimentalApi::class)
    public fun emit(
        body: Any? = null,
        eventName: String? = null,
        timestamp: Long = 0,
        observedTimestamp: Long = 0,
        context: Context? = null,
        severityNumber: SeverityNumber? = null,
        severityText: String? = null,
        exception: Throwable? = null,
        attributes: (AttributesMutator.() -> Unit)? = null,
    )
}
