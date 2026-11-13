package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Supplies the SDK dependencies needed to construct processors and exporters for any signal.
 */
@ExperimentalApi
@ConfigDsl
public interface ExportConfigDsl {

    /**
     * The [Clock] implementation that will be used by the OpenTelemetry implementation.
     */
    public val clock: Clock

    /**
     * The [SdkErrorHandler] used to report errors and misuse of the SDK.
     */
    public val sdkErrorHandler: SdkErrorHandler
}
