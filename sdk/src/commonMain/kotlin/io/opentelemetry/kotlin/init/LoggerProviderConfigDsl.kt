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
     * Adds a [LogRecordProcessor] to the logger provider. Processors will be invoked
     * in the order in which they were added.
     */
    @Deprecated("Deprecated.", ReplaceWith("export {processor}"))
    public fun addLogRecordProcessor(processor: LogRecordProcessor)

    /**
     * The log limits configuration for this logger provider.
     */
    public fun logLimits(action: LogLimitsConfigDsl.() -> Unit)

    /**
     * Configures how log records should be processed and exported.
     */
    public fun export(action: LogExportConfigDsl.() -> LogRecordProcessor)
}
