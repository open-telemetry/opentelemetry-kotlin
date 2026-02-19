package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Configures how log records are exported.
 */
@ExperimentalApi
@ConfigDsl
public interface LogExportConfigDsl {

    /**
     * The [Clock] implementation that will be used by the OpenTelemetry implementation.
     */
    public val clock: Clock
}
