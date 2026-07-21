package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Configures how traces are exported.
 */
@ExperimentalApi
@ConfigDsl
public interface TraceExportConfigDsl {

    /**
     * The [Clock] implementation that will be used by the OpenTelemetry implementation.
     */
    public val clock: Clock

    /**
     * The [SdkErrorHandler] used to report errors and misuse of the SDK.
     */
    public val sdkErrorHandler: SdkErrorHandler
}
