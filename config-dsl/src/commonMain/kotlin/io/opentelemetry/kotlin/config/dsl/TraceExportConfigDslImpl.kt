package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.init.TraceExportConfigDsl

/**
 * Supplies the SDK dependencies that span processors need when they are constructed via the DSL.
 */
@ExperimentalApi
class TraceExportConfigDslImpl(
    override val clock: Clock,
    override val sdkErrorHandler: SdkErrorHandler,
) : TraceExportConfigDsl
