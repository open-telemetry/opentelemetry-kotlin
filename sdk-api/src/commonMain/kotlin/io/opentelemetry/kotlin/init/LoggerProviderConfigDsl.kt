package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor

/**
 * Defines configuration for the [io.opentelemetry.kotlin.logging.LoggerProvider].
 */
@ExperimentalApi
@ConfigDsl
public interface LoggerProviderConfigDsl : ResourceConfigDsl {

    /**
     * The log limits configuration for this logger provider.
     */
    public fun logLimits(action: LogLimitsConfigDsl.() -> Unit)

    /**
     * Configures how log records should be processed and exported.
     */
    public fun export(action: LogExportConfigDsl.() -> LogRecordProcessor)
}
