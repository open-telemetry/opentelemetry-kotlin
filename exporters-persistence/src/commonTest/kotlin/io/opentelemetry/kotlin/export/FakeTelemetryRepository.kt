package io.opentelemetry.kotlin.export

internal class FakeTelemetryRepository<T>(
    var storeFails: Boolean = false,
) : TelemetryRepository<T> {

    var storeCalls = 0
    var deleteCalls = 0
    var storedTelemetry: MutableList<List<T>> = mutableListOf()

    override fun store(telemetry: List<T>): PersistedTelemetryRecord? {
        storeCalls++
        storedTelemetry += telemetry
        if (storeFails) {
            return null
        }
        return PersistedTelemetryRecord(
            timestamp = storeCalls.toLong(),
            type = PersistedTelemetryType.LOGS,
            uid = "fake-$storeCalls",
        )
    }

    override fun poll(): PersistedTelemetryRecord? = null
    override fun read(record: PersistedTelemetryRecord): List<T>? = null

    override fun delete(record: PersistedTelemetryRecord) {
        deleteCalls++
    }
}
