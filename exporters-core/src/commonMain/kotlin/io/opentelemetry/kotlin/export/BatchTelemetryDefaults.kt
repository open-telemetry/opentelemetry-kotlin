package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Default values for how telemetry is batched.
 */
@ExperimentalApi
public object BatchTelemetryDefaults {

    /**
     * The maximum queue size before a flush is triggered.
     */
    public const val MAX_QUEUE_SIZE: Int = 2048

    /**
     * Delay in ms before a flush is triggered
     */
    public const val SCHEDULE_DELAY_MS: Long = 1000

    /**
     * Timeout in ms for exporting telemetry
     */
    public const val EXPORT_TIMEOUT_MS: Long = 30000

    /**
     * Maximum number of telemetry items to export in a single batch
     */
    public const val MAX_EXPORT_BATCH_SIZE: Int = 512
}
