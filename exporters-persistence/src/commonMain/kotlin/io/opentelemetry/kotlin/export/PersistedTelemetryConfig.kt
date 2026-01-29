package io.opentelemetry.kotlin.export

internal class PersistedTelemetryConfig(

    /**
     * Maximum number of batches that should be stored for each telemetry signal. 100 by default.
     * For example, 100 batches of X logs may be stored before old telemetry is deleted.
     */
    val maxBatchedItemsPerSignal: Int = 100,

    /**
     * Maximum age of telemetry before it should be deleted. Old telemetry is not considered useful
     * so it can be deleted after 30 days by default.
     */
    val maxTelemetryAgeInDays: Long = 30,
)
