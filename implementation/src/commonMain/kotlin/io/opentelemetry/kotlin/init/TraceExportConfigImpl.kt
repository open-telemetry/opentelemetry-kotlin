package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.error.SdkErrorHandler

internal class TraceExportConfigImpl(
    override val clock: Clock,
    override val sdkErrorHandler: SdkErrorHandler,
) : TraceExportConfigDsl
