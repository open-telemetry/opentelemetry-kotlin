package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer

/**
 * A read-write representation of a log record.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#readablelogrecord
 */
@ExperimentalApi
public interface ReadWriteLogRecord : ReadableLogRecord, MutableAttributeContainer {

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
     * Contains the body of the log message - i.e. a human-readable string or free-form string data.
     */
    public override var body: String?

    /**
     * Contains the event name if this is an event, otherwise null
     */
    public override var eventName: String?
}
