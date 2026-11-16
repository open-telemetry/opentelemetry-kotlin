package io.opentelemetry.kotlin.semconv.events

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.SeverityNumber

/**
 * A semantic convention event that can be emitted as a log record.
 */
public interface OpenTelemetryEvent {

    /**
     * Emits this event via [logger], using the event's semantic convention name and attributes.
     *
     * - [timestamp] - the time the event occurred, in nanoseconds since the Unix epoch. Defaults to 0, which
     *   means unknown; the timestamp is then left unset. Any value <= 0 is treated as unknown.
     * - [observedTimestamp] - the time the event was observed, in nanoseconds since the Unix epoch. Set this
     *   when emitting an event later than it was observed. Defaults to 0, which means the current time. Any
     *   value <= 0 is treated as the current time.
     * - [severityNumber] - the severity of the event
     * - [context] - the context in which the event was emitted
     * - [attributes] - additional attributes to associate with the event. These are applied after the
     *   event's own attributes, so they replace any with the same key.
     */
    @OptIn(ExperimentalApi::class)
    public fun emit(
        logger: Logger,
        timestamp: Long = 0,
        observedTimestamp: Long = 0,
        severityNumber: SeverityNumber? = null,
        context: Context? = null,
        attributes: (AttributesMutator.() -> Unit)? = null,
    )
}
