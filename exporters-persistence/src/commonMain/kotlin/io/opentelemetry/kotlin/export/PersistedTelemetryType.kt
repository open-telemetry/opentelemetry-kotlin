package io.opentelemetry.kotlin.export

/**
 * The type of telemetry that is persisted.
 */
internal enum class PersistedTelemetryType(val directoryName: String) {
    LOGS("logs"),
    SPANS("spans"),
}
