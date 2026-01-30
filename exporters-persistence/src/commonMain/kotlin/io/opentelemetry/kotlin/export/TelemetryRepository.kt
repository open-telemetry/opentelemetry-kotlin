package io.opentelemetry.kotlin.export

/**
 * Implementations store telemetry so that it isn't lost in the case of process termination.
 */
internal interface TelemetryRepository<T> {

    /**
     * Stores the telemetry collection in one location.
     *
     * @return a record describing the telemetry, or null if the telemetry could not be stored
     * for whatever reason.
     */
    fun store(telemetry: List<T>): PersistedTelemetryRecord?

    /**
     * Retrieves the next telemetry record that should be sent, if any exists.
     */
    fun poll(): PersistedTelemetryRecord?

    /**
     * Reads and deserializes the telemetry from the given record.
     *
     * @return the deserialized telemetry, or null if the record could not be read
     */
    fun read(record: PersistedTelemetryRecord): List<T>?

    /**
     * Deletes the given record from the repository, if it is present.
     */
    fun delete(record: PersistedTelemetryRecord)
}
