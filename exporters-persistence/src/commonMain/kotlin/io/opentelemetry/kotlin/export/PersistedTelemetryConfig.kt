package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.config.validateOrUseDefault
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

internal class PersistedTelemetryConfig(
    maxBatchedItemsPerSignal: Int = DEFAULT_MAX_BATCHED_ITEMS_PER_SIGNAL,
    maxTelemetryAgeInDays: Long = DEFAULT_MAX_TELEMETRY_AGE_IN_DAYS,
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) {
    /**
     * Maximum number of batches that should be stored for each telemetry signal. 100 by default.
     * For example, 100 batches of X logs may be stored before old telemetry is deleted.
     */
    val maxBatchedItemsPerSignal: Int = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "maxBatchedItemsPerSignal",
        value = maxBatchedItemsPerSignal,
        default = DEFAULT_MAX_BATCHED_ITEMS_PER_SIGNAL,
    ) { it > 0 }

    /**
     * Maximum age of telemetry before it should be deleted. Old telemetry is not considered useful
     * so it can be deleted after 30 days by default.
     */
    val maxTelemetryAgeInDays: Long = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "maxTelemetryAgeInDays",
        value = maxTelemetryAgeInDays,
        default = DEFAULT_MAX_TELEMETRY_AGE_IN_DAYS,
    ) { it > 0 }

    private companion object {
        const val API = "PersistedTelemetryConfig"
        const val DEFAULT_MAX_BATCHED_ITEMS_PER_SIGNAL = 100
        const val DEFAULT_MAX_TELEMETRY_AGE_IN_DAYS = 30L
    }
}
