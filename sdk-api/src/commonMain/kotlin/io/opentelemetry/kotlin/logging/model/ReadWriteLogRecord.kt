package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.logging.SeverityNumber

/**
 * A read-write representation of a log record.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#readablelogrecord
 */
@ExperimentalApi
public interface ReadWriteLogRecord : ReadableLogRecord, AttributesMutator, AttributeContainer {

    /**
     * The timestamp in nanoseconds at which the event occurred.
     */
    public override var timestamp: Long?

    /**
     * The timestamp in nanoseconds at which the event was entered into the OpenTelemetry API.
     */
    public override var observedTimestamp: Long?

    /**
     * The severity of the log.
     */
    public override var severityNumber: SeverityNumber?

    /**
     * A string representation of the severity at the point it was captured. This can be distinct from
     * [SeverityNumber] - for example, when capturing logs from a 3rd party library with different severity concepts.
     */
    public override var severityText: String?

    /**
     * Contains the body of the log message. Can be a string, number, boolean, map, or list,
     * per the OpenTelemetry log data model (https://opentelemetry.io/docs/specs/otel/logs/data-model/#field-body).
     */
    public override var body: Any?

    /**
     * Contains the event name if this is an event, otherwise null
     */
    public override var eventName: String?
}
